///////////////////////////////////////////////////////////////////////////////
efw.server.event.prepare=function(event,params){
	//the login check
	//-------------------------------------------------------------------------
	var needlogincheck=efw.server.properties.getBoolean("efw.login.check");
	var loginkey=efw.server.properties.get("efw.login.key");
	if (needlogincheck && !event.outoflogin){
		var vl=efw.server.session.get(loginkey);
		if (vl==null||vl==""){
			throw {error:{errorType:"SessionTimeoutException",canContinue:true}};
		}
	}
	//open database
	//-------------------------------------------------------------------------
	efw.server.db.open();

	//add your program here
	//-------------------------------------------------------------------------
	//TODO
	return null;
};
///////////////////////////////////////////////////////////////////////////////
efw.server.event.finish=function(event,params,result){
	//close database
	//-------------------------------------------------------------------------
	if (result){
		efw.server.db.commit();
	}else{
		efw.server.db.rollback();
	}
	efw.server.db.close();
	
	//add your program here
	//-------------------------------------------------------------------------
	//TODO
	
};
