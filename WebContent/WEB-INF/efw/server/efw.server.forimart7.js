///////////////////////////////////////////////////////////////////////////////
//the function will be called by html
//==========================================================
function doPost(req){
	var eventId=req.eventId;
	var params=req.params;

	load("efw/event/"+eventId);
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
		return event.paramsformat;
	}else{
		//return efw.server.event.fire(event,params);
		return event.fire(params);
	}
}

///////////////////////////////////////////////////////////////////////////////
efw = {};
efw.server={};
efw.server.db={};
//change recordset to java array
efw.server.db.executeQuery=function(oParam){
	var groupId=oParam.groupId;
	var sqlId=oParam.sqlId;
	var params=oParam.params;
	var mapping=oParam.mapping;

	var oSql=Packages.efw.sql.SqlManager.get(groupId,sqlId);
	var mp=new java.util.HashMap();
	for (var key in params){mp.put(key,params[key]);}
	var sql=oSql.getSqlString(mp)+"";
	var pms=oSql.getSqlParams(mp);
	var parameters=efw.server.db.getDbParameters(pms);
	var result=DatabaseManager.select(sql,parameters);

	if (result.error){
		throw result.errorMessage;
	}

	var ret=[];
	for (var i=0;i<result.data.length;i++){
		var rs=result.data[i];
		var item={};
		if (mapping!=null){
			for(var key in mapping){
				var vl=rs[mapping[key]];
				item[key]=vl;
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

	var oSql=Packages.efw.sql.SqlManager.get(groupId,sqlId);
	var mp=new java.util.HashMap();
	for (var key in params){mp.put(key,params[key]);}
	var sql=oSql.getSqlString(mp)+"";
	var parameters=efw.server.db.getDbParameters(oSql.getSqlParams(mp));

	var result=DatabaseManager.execute(sql,parameters);
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
	
	var oSql=Packages.efw.sql.SqlManager.get(groupId,sqlId);
	var mp=new java.util.HashMap();
	for (var key in params){mp.put(key,params[key]);}
	var sql=oSql.getSqlString(mp)+"";
	var parameters=efw.server.db.getDbParameters(oSql.getSqlParams(mp));
	
	var result=DatabaseManager.execute(sql,parameters);
	if (result.error){
		throw result.errorMessage;
	}
};
efw.server.db.rollback=function(){
	DatabaseManager.rollback();
};
efw.server.db.commit=function(){
	DatabaseManager.commit();
};


//==========================================================
efw.server.db.getDbParameters= function(aryParam){
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
		if (value==null){
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
