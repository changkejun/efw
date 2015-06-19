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
	var result;
	if (params==null){
		result=event.paramsformat;
	}else{
		result=efw.server.event.fire(event,params);
	}
	return JSON.stringify(result);
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
		return e;
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
	var params=oParam.params;
	var mapping=oParam.mapping;

	if (_engine.getFactory().getEngineName()=="Mozilla Rhino"){//java 1.6 1.7
		var mp=new java.util.HashMap();
		for (var key in params){mp.put(key,params[key]);}
		params=mp;
	}
	var rs= _database.executeQuery(groupId,sqlId,params);
	var ret=[];
	var meta=rs.getMetaData();
	var parseValue=function(vl){
		if (_engine.getFactory().getEngineName()=="Mozilla Rhino"){//java 1.6 1.7
			var value = vl;
		    if (typeof value =="object"){
				if (value==null){
					value=null;
				}else if(value.getClass().getName()=="java.lang.String"){
					value="" + value;
				}else if(value.getClass().getName()=="java.lang.Boolean"){
					value= true && value;
				}else if(value.getClass().getName()=="java.lang.Byte"){
					value=0+new Number(value);
				}else if(value.getClass().getName()=="java.lang.Short"){
					value=0+new Number(value);
				}else if(value.getClass().getName()=="java.lang.Integer"){
					value=0+new Number(value);
				}else if(value.getClass().getName()=="java.lang.Long"){
					value=0+new Number(value);
				}else if(value.getClass().getName()=="java.lang.Float"){
					value=0+new Number(value);
				}else if(value.getClass().getName()=="java.lang.Double"){
					value=0+new Number(value);
				}else if(value.getClass().getName()=="java.math.BigDecimal"){
					value=0+new Number(value);
				}else if(value.getClass().getName()=="java.sql.Date"){
					value=new Date(value);
				}else if(value.getClass().getName()=="java.sql.Time"){
					value=new Date(value);
				}else if(value.getClass().getName()=="java.sql.Timestamp"){
					value=new Date(value);
				}else{
					// you should do something if the comment is printed out.
					java.lang.System.out.println(value + " is an instance of "+value.getClass().getName());
				}
			}
			vl=value;
		}
		return vl;
	};
	while (rs.next()) {
		var item={};
		if (mapping!=null){
			for(var key in mapping){
				item[key]=parseValue(rs.getObject(mapping[key]));
			}
		}else{
			var maxColumnCount=meta.getColumnCount();
			for (var j=1;j<=maxColumnCount;j++){
				var key=meta.getColumnName(j);
				item[key]=parseValue(rs.getObject(key));
			}
		}
		ret.push(item);
	}
	rs.close();
	return ret;
};
efw.server.db.executeUpdate=function(oParam){
	var groupId=oParam.groupId;
	var sqlId=oParam.sqlId;
	var params=oParam.params;
	return _database.executeUpdate(groupId,sqlId,params);
};
efw.server.db.execute=function(oParam){
	var groupId=oParam.groupId;
	var sqlId=oParam.sqlId;
	var params=oParam.params;
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
