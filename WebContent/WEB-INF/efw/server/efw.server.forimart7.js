/**
 * efw framework server library for imart7
 * @author Chang Kejun
 */
///////////////////////////////////////////////////////////////////////////////
function doPost(req){
	var eventId=req.eventId;
	var params=req.params;

	(function doInclude(eventId){
		load("efw/event/"+eventId);
		var event=eval("("+eventId+")");
		var include=event.include;
		if (include!=null){
			for(var i=0;i<include.length;i++){
				doInclude(include[i].eventId);
			}
		}
	})(eventId);
	var event=eval("("+eventId+")");
	
	if (params==null){
		var result=event.paramsformat;
		var include=event.include;
		if (include!=null){
			for(var i=0;i<include.length;i++){
				if (include[i].mergeParamsformat){
					var eventsub=eval("("+include[i].eventId+")");
					var subresult=eventsub.paramsformat;
					for (var key in subresult){
						result[key]=subresult[key];//only the top layer,not into sub layer.
					}
				}
			}
		}
		return result;
	}else{
		try{
			var result=event.fire(params);
			efw.server.db.commit();
			return result;
		}catch(e){
			efw.server.db.rollback();
			throw e;
		}
	}
}

///////////////////////////////////////////////////////////////////////////////
var isDebug=true && Packages.efw.properties.PropertiesManager.getBooleanProperty("efw.isdebug",false);

efw = {};
efw.server={};
efw.server.db={};
//change recordset to java array
efw.server.db.executeQuery=function(oParam){
	var groupId=oParam.groupId;
	var sqlId=oParam.sqlId;
	var params=oParam.params;
	var mapping=oParam.mapping;
	var connectId=oParam.connectId;
	var isGroup=oParam.isGroup;


	var oSql=Packages.efw.sql.SqlManager.get(groupId,sqlId);
	var mp=new java.util.HashMap();
	for (var key in params){mp.put(key,params[key]);}
	var sql=oSql.getSqlString(mp)+"";
	var pms=oSql.getSqlParams(mp);
	var parameters=efw_server_db_getDbParameters(pms);
	var result=DatabaseManager.select(sql,parameters,0,connectId,isGroup);

	if (isDebug){
		Debug.print("==========efw.server.db.executeQuery==========");
		Debug.print(sql);
		Debug.console(pms,result.data);
	}

	if (result.error){
		throw result.errorMessage;
	}

	var ret=[];
	for (var i=0;i<result.data.length;i++){
		var rs=result.data[i];
		var item={};
		if (mapping!=null){
			for(var key in mapping){
				var mp=mapping[key];
				if(typeof mp =="string"){
					var vl=rs[mp];
					item[key]=vl;
				}else if(typeof mp =="function"){
					var vl=mp(rs);
					item[key]=vl;
				}
			}
		}else{
			item=rs;
		}
		ret[i]=item;
	}
	
	return ret;

};
//==========================================================
efw.server.db.executeUpdate=function(oParam){
	var groupId=oParam.groupId;
	var sqlId=oParam.sqlId;
	var params=oParam.params;
	var connectId=oParam.connectId;
	var isGroup=oParam.isGroup;

	var oSql=Packages.efw.sql.SqlManager.get(groupId,sqlId);
	var mp=new java.util.HashMap();
	for (var key in params){mp.put(key,params[key]);}
	var sql=oSql.getSqlString(mp)+"";
	var pms=oSql.getSqlParams(mp);
	var parameters=efw_server_db_getDbParameters(pms);
	var result=DatabaseManager.execute(sql,parameters,connectId,isGroup);

	if (isDebug){
		Debug.print("==========efw.server.db.executeQuery==========");
		Debug.print(sql);
		Debug.console(pms,result.data);
	}

	if (result.error){
		throw result.errorMessage;
	}
	return result.countRow;
};
//==========================================================
efw.server.db.execute=function(oParam){
	var groupId=oParam.groupId;
	var sqlId=oParam.sqlId;
	var params=oParam.params;
	var connectId=oParam.connectId;
	var isGroup=oParam.isGroup;
	
	var oSql=Packages.efw.sql.SqlManager.get(groupId,sqlId);
	var mp=new java.util.HashMap();
	for (var key in params){mp.put(key,params[key]);}
	var sql=oSql.getSqlString(mp)+"";
	var pms=oSql.getSqlParams(mp);
	var parameters=efw_server_db_getDbParameters(pms);
	var result=DatabaseManager.execute(sql,parameters,connectId,isGroup);

	if (isDebug){
		Debug.print("==========efw.server.db.executeQuery==========");
		Debug.print(sql);
		Debug.console(pms,result.data);
	}
	
	if (result.error){
		throw result.errorMessage;
	}
};
//==========================================================
efw.server.db.rollback=function(){
	DatabaseManager.rollback();
};
//==========================================================
efw.server.db.commit=function(){
	DatabaseManager.commit();
};
//==========================================================
function efw_server_db_getDbParameters(aryParam){
	var ret=new Array();
	for (var i=0;i<aryParam.size();i++){
		var value=aryParam.get(i);
		if (typeof value =="object"){//if the value is object, it means the value is from database
			if (value==null){
				value=null;
			}else if(value.getClass().getName()=="java.lang.Double"){
				value=0+new Number(value);
			}else if(value.getClass().getName()=="java.lang.String"){
				value="" + value;
			}else{
				// you should do something about the comment
				Debug.print(value + " is an instance of "+value.getClass().getName());
			}
		}

		//imart db can only use the four parameter types.
		if (value==null||value==""){
			ret[i] = new DbParameter(null, DbParameter.TYPE_STRING);
        }else if( isNumber(value)){
            ret[i] = new DbParameter(value, DbParameter.TYPE_NUMBER);
        }else if( isDate(value)){
            ret[i] = new DbParameter(value, DbParameter.TYPE_DATE);
        }else if(isString(value)){
            ret[i] = new DbParameter(value, DbParameter.TYPE_STRING);
        }else if( isBoolean( value ) ){
            if( value ) {
                ret[i] = new DbParameter("1", DbParameter.TYPE_STRING);
            }else{
                ret[i] = new DbParameter("0", DbParameter.TYPE_STRING);
            }
        }
	}
	return ret;
}
