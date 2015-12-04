/**
 * efw framework server library
 * @author Chang Kejun
 */
///////////////////////////////////////////////////////////////////////////////
var _request;
var _serverfolder;
var _eventfolder;
var _isdebug;
var _database;
var _engine;// Mozilla Rhino 1.6 1.7 / Oracle Nashorn 1.8
///////////////////////////////////////////////////////////////////////////////
function doPost(req){
	var eventId=req.eventId;
	var params=req.params;
	var needLoad=false;
	if (!_isdebug){
		if (!efw.server.events[eventId]){
			needLoad=true;
		}
	}else{
		needLoad=true;
	}
	if(needLoad){
		(function doInclude(eventId){
			load(_eventfolder+"/"+eventId+".js");
			var event=eval("("+eventId+")");
			var include=event.include;
			if (include!=null){
				for(var i=0;i<include.length;i++){
					doInclude(include[i].eventId);
				}
			}
			efw.server.events[eventId]=event;
		})(eventId);
	}
	var event=efw.server.events[eventId];
	var paramsformat=event.paramsformat;
	var include=event.include;
	if (include!=null){
		for(var i=0;i<include.length;i++){
			if (include[i].mergeParamsformat){
				var eventsub=efw.server.events[include[i].eventId];
				var subparamsformat=eventsub.paramsformat;
				for (var key in subparamsformat){
					paramsformat[key]=subparamsformat[key];//only the top layer,not into sub layer.
				}
			}
		}
	}
	if (params==null){
		return JSON.stringify(paramsformat);
	}else{
		function formatParams(pms,fts){
			for(var key in fts){
				var format=fts[key];
				var param=pms[key];
				if(param!=null&&param!=""){
					if(typeof format =="string"){
						if(format.indexOf("#")>-1||format.indexOf("0")>-1){//number #,##0.0
							var num;
							try{
								num=0+new Number(Packages.efw.format.FormatManager.parseNumber(param,format));
							}catch(e){
								Packages.efw.log.LogManager.WariningDebug("Input value ["+ param + "] is not matched to format ["+format+"]","");
								num=null;
							}
							pms[key]=num;
						}else{//date YYYY/MM/DD
							var dt=new Date();
							try{
								dt.setTime(Packages.efw.format.FormatManager.parseDate(param,format).getTime());
							}catch(e){
								Packages.efw.log.LogManager.WariningDebug("Input value ["+ param + "] is not matched to format ["+format+"]","");
								dt=null;
							}
							pms[key]=dt;
						}
					}else if(Array.isArray(format)){
						for(var i=0;i<param.length;i++){
							formatParams(param[i],format[0]);
						}
					}else if(typeof format ==="object"){
						formatParams(param,format);
					}
				}
			}
		};
		formatParams(params,paramsformat);
		return JSON.stringify(efw.server.event.fire(event,params));
	}
}
///////////////////////////////////////////////////////////////////////////////
var efw = {};
efw.server={};
efw.server.event={};
efw.server.events={};
efw.server.session={};
efw.server.properties={};
efw.server.db={};
///////////////////////////////////////////////////////////////////////////////
if (_engine.getFactory().getEngineName()=="Mozilla Rhino"){//java 1.6 1.7
	function load(filename) { Packages.efw.script.ScriptManager.load(filename);}
	if(_engine.getFactory().getEngineVersion()<"1.7"){//java 1.6
		load(_serverfolder+"/json2.min.js");
	}
}
load(_serverfolder+"/efw.server.customize.js");
///////////////////////////////////////////////////////////////////////////////
efw.server.session.get=function(key){
	return _request.getSession().getAttribute(key);
};
efw.server.session.set=function(key,value){
	_request.getSession().setAttribute(key,value);
};
///////////////////////////////////////////////////////////////////////////////
efw.server.properties.get=function(key,defaultValue){
	var dv;
	if (defaultValue==undefined){
		dv=null;
	}else{
		dv=defaultValue+"";
	}
	return Packages.efw.properties.PropertiesManager.getProperty(key,dv);
};
efw.server.properties.getBoolean=function(key,defaultValue){
	var dv;
	if (defaultValue==undefined){
		dv=false;
	}else{
		dv=defaultValue && true;
	}
	return Packages.efw.properties.PropertiesManager.getBooleanProperty(key,dv);
};
efw.server.properties.getInt=function(key,defaultValue){
	var dv;
	if (defaultValue==undefined){
		dv=0;
	}else{
		dv=defaultValue +0;
	}
	return Packages.efw.properties.PropertiesManager.getIntProperty(key,dv);
};
///////////////////////////////////////////////////////////////////////////////
efw.server.event.fire=function(event,params){
	try{
		efw.server.event.prepare(event,params);
		var result=event.fire(params);
		try{efw.server.event.finish(event,params,result);}catch(e2){}
		return result;
	}catch(e){
		try{efw.server.event.finish(event,params,null);}catch(e2){}
		if(typeof e =="object"){
			if (e.error) return e;
		}
		throw e;
	}
};
///////////////////////////////////////////////////////////////////////////////
efw.server.db.open=function(jdbcResourceName){
	if (jdbcResourceName==null){
		_database=Packages.efw.db.DatabaseManager.open();
	}else{
		_database=Packages.efw.db.DatabaseManager.open(jdbcResourceName);
	}
};
//change recordset to java array
efw.server.db.executeQuery=function(oParam){
	var groupId=oParam.groupId;
	var sqlId=oParam.sqlId;
	var params=efw_server_db_getDbParameters(oParam.params);
	var mapping=oParam.mapping;
	
	var rs= _database.executeQuery(groupId,sqlId,params);
	var ret=[];
	var meta=rs.getMetaData();
	var parseValue=function(vl){
		var value = vl;
	    if (typeof value =="object"){
			if (value==null){
				value=null;
			}else if(value.getClass().getName()=="java.lang.String"){
				value="" + value;
			}else if(value.getClass().getName()=="java.lang.Boolean"){
				value= true && value;
			}else if(value.getClass().getName()=="java.lang.Byte"
				||value.getClass().getName()=="java.lang.Short"
				||value.getClass().getName()=="java.lang.Integer"
				||value.getClass().getName()=="java.lang.Long"
				||value.getClass().getName()=="java.lang.Float"
				||value.getClass().getName()=="java.lang.Double"
				||value.getClass().getName()=="java.math.BigDecimal"){
				value=0+new Number(value);
			}else if(value.getClass().getName()=="java.sql.Date"
				||value.getClass().getName()=="java.sql.Time"
				||value.getClass().getName()=="java.sql.Timestamp"){
				var dt=new Date();dt.setTime(value.getTime());value=dt;
			}else{
				// you should do something if the comment is printed out.
				Packages.efw.log.LogManager.ErrorDebug("["+value + "] is an instance of "+value.getClass().getName()+" which has not been supported by efw.","");
			}
		}
		return value;
	};
	while (rs.next()) {
		var item={};
		var rsdata={};
		var maxColumnCount=meta.getColumnCount();
		for (var j=1;j<=maxColumnCount;j++){
			var key=meta.getColumnName(j);
			rsdata[key]=parseValue(rs.getObject(key));
		}

		if (mapping!=null){
			for(var key in mapping){
				var mp=mapping[key];
				if(typeof mp =="string"){
					var vl=rsdata[mp];
					item[key]=vl;
				}else if(typeof mp =="function"){
					var vl=mp(rsdata);
					item[key]=vl;
				}else if(typeof mp =="object" && Array.isArray(mp)){
					var vl=rsdata[mp[0]];
					var ft=mp[1];
					if(vl!=null&&ft!=null){
						if(vl.toFixed){//if vl is number #,##0.00
							vl=""+Packages.efw.format.FormatManager.formatNumber(vl,ft);
						}else if(vl.getTime){//if vl is date yyyyMMdd
							vl=""+Packages.efw.format.FormatManager.formatDate(vl.getTime(),ft);
						}
						//if vl is not date or number, it should not have format
					}
					item[key]=vl;
				}
			}
		}else{
			item=rsdata;
		}
		ret.push(item);
	}
	rs.close();
	return ret;
};
efw.server.db.executeUpdate=function(oParam){
	var groupId=oParam.groupId;
	var sqlId=oParam.sqlId;
	var params=efw_server_db_getDbParameters(oParam.params);
	return _database.executeUpdate(groupId,sqlId,params);
};
efw.server.db.execute=function(oParam){
	var groupId=oParam.groupId;
	var sqlId=oParam.sqlId;
	var params=efw_server_db_getDbParameters(oParam.params);
	_database.execute(groupId,sqlId,params);
};
efw.server.db.rollback=function(){
	_database.rollback();
};
efw.server.db.commit=function(){
	_database.commit();
};
efw.server.db.close=function(){
	_database.close();
};

function efw_server_db_getDbParameters(aryParam){
	var params={};
	for(var key in aryParam){//change blank to null for simple sql just like oracle
		var vl=aryParam[key];
		if (vl=="")vl=null;
		params[key]=vl;
	}
	if (_engine.getFactory().getEngineName()=="Mozilla Rhino"){//java 1.6 1.7
		var mp=new java.util.HashMap();
		for (var key in params){mp.put(key,params[key]);}
		params=mp;
	}
	return params;
}