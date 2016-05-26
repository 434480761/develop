<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>第三方系统注册服务表</title>
    <script src="../js/jquery-1.8.3.min.js"></script>
<style>

body {
    width: 1000px;
    margin: 40px auto;
    font-family: 'trebuchet MS', 'Lucida sans', Arial;
    font-size: 14px;
    color: #444;
}

table {
    *border-collapse: collapse; /* IE7 and lower */
    border-spacing: 0;
    width: 100%;    
}

.bordered {
    border: solid #ccc 1px;
    -moz-border-radius: 6px;
    -webkit-border-radius: 6px;
    border-radius: 6px;
    -webkit-box-shadow: 0 1px 1px #ccc; 
    -moz-box-shadow: 0 1px 1px #ccc; 
    box-shadow: 0 1px 1px #ccc;         
}

.bordered tr:hover {
    background: #fbf8e9;
    -o-transition: all 0.1s ease-in-out;
    -webkit-transition: all 0.1s ease-in-out;
    -moz-transition: all 0.1s ease-in-out;
    -ms-transition: all 0.1s ease-in-out;
    transition: all 0.1s ease-in-out;     
}    
    
.bordered td, .bordered th {
    border-left: 1px solid #ccc;
    border-top: 1px solid #ccc;
    padding: 10px;
    text-align: left;    
}

.bordered th {
    background-color: #dce9f9;
    background-image: -webkit-gradient(linear, left top, left bottom, from(#ebf3fc), to(#dce9f9));
    background-image: -webkit-linear-gradient(top, #ebf3fc, #dce9f9);
    background-image:    -moz-linear-gradient(top, #ebf3fc, #dce9f9);
    background-image:     -ms-linear-gradient(top, #ebf3fc, #dce9f9);
    background-image:      -o-linear-gradient(top, #ebf3fc, #dce9f9);
    background-image:         linear-gradient(top, #ebf3fc, #dce9f9);
    -webkit-box-shadow: 0 1px 0 rgba(255,255,255,.8) inset; 
    -moz-box-shadow:0 1px 0 rgba(255,255,255,.8) inset;  
    box-shadow: 0 1px 0 rgba(255,255,255,.8) inset;        
    border-top: none;
    text-shadow: 0 1px 0 rgba(255,255,255,.5); 
}

.bordered td:first-child, .bordered th:first-child {
    border-left: none;
}

.bordered th:first-child {
    -moz-border-radius: 6px 0 0 0;
    -webkit-border-radius: 6px 0 0 0;
    border-radius: 6px 0 0 0;
}

.bordered th:last-child {
    -moz-border-radius: 0 6px 0 0;
    -webkit-border-radius: 0 6px 0 0;
    border-radius: 0 6px 0 0;
}

.bordered th:only-child{
    -moz-border-radius: 6px 6px 0 0;
    -webkit-border-radius: 6px 6px 0 0;
    border-radius: 6px 6px 0 0;
}

.bordered tr:last-child td:first-child {
    -moz-border-radius: 0 0 0 6px;
    -webkit-border-radius: 0 0 0 6px;
    border-radius: 0 0 0 6px;
}

.bordered tr:last-child td:last-child {
    -moz-border-radius: 0 0 6px 0;
    -webkit-border-radius: 0 0 6px 0;
    border-radius: 0 0 6px 0;
}
</style>
</head>
<body>
<h2>第三方系统注册服务表</h2>
<form>
  <fieldset>
    <legend>系统注册</legend>
      业务系统：<input id="bsysname" type="text" />
      管理员：<input  id="bsysadmin" type="text" />
      <input id="register" type="button" value="注册"/>
  </fieldset>
</form>
<br>
<table class="bordered">
    <thead>
    <tr>
        <th>Service Key</th>        
        <th>Bussiness System</th>
        <th>Bussiness Admin</th>
        <th>Operation</th>
    </tr>
    </thead>
    <tbody id="resultTbody"></tbody>
</table>
</body>
<script type="text/javascript">
var bearerToken = "DEBUG userid= 2000284696,realm=lc.service.esp.nd";

$(function(){
	getToken();
	reflashPage();
})

function reflashPage(){
	$.ajax({
		url:"../v0.6/3dbsys/servicekey?limit=(0,1000)",
		data:"",
		async:true,
		type:'GET',
		dataType:"json",
		success:function(data){
			if(data != null){
				showResultList(data);	
			}
		},
	});
}

$("#register").click(function(){
	var bsysname = $("#bsysname").val();
	var bsysadmin = $("#bsysadmin").val();
	var json = "{\"bsysname\":\"" + bsysname + "\",\"bsysadmin\":\"" + bsysadmin + "\",\"bsysivcconfig\":{\"global_load\": {\"max_rps\": \"200\",\"max_dpr\": \"100000\"}}}"
	$.ajax({
		url:"../v0.6/3dbsys/servicekey/registry",
		headers:{"Authorization":bearerToken,"bsyskey":"DEFAULT_SERVICE_KEY"},
		data:json,
		async:false,
		type:'POST',
		contentType:"application/json",
		dataType:"json",
	}).done(function(msg){
		reflashPage();
	});
});

function delService(id){
	$.ajax({
		url:"../v0.6/3dbsys/servicekey/"+id,
		headers:{"Authorization":bearerToken,"bsyskey":"DEFAULT_SERVICE_KEY"},
		data:"",
		async:false,
		type:'DELETE',
		contentType:"application/json",
		dataType:"json",
	}).done(function(msg){
		reflashPage();
	});
}

function showResultList(data){
	var resultTbody = $("#resultTbody");
	resultTbody.empty();
	$.each(data.items,function(i,service){
		var $tr = $("<tr>").appendTo(resultTbody);
		var $td0 = $("<td>").appendTo($tr);
		var $td1 = $("<td>").appendTo($tr);
		var $td2 = $("<td>").appendTo($tr);
		var $td3 = $("<td>").appendTo($tr);
		$td0.html(service.bsyskey);
		$td1.html(service.bsysname);
		$td2.html(service.bsysadmin);
		$td3.html("<form><input type=\"button\" id=\""+ service.identifier +"\" value=\"编辑\" /> <input type=\"button\" id=\"del" + i + "\" value=\"删除\" /></form>");
		$("#"+service.identifier).click(function(){
			location.href = "authConfigDetail.jsp?identifier="+service.identifier;
		});
		$("#del"+i).click(function(){
			delService(service.identifier);
		});
	})
}

function getToken(){
	if(location.href.match("esp-lifecycle.web.sdp.101.com")!=null) {
		ucUrl = "https://aqapi.101.com/v0.93/bearer_tokens";
	} else {
		ucUrl = "https://ucbetapi.101.com/v0.93/bearer_tokens";
	}
	$.ajax({
		url:ucUrl,
		data:"{\"login_name\":\"esp_lifecycle\",\"password\":\"d4876ded8c0df211825893ae8a3c6df9\"}",
		async:false,
		type:'POST',
		contentType:"application/json",
		dataType:"json",
		success:function(data){
			if(data != null){
				bearerToken = "Bearer \"" + data.access_token +"\"";
			}
		},
	});
}
</script>
</html>