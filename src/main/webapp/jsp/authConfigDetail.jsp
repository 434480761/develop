<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>第三方系统注册服务配置信息详情</title>
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
<h2>第三方系统注册服务配置信息详情</h2>
<form>
  <fieldset>
    <legend>配置信息</legend>
      <p>业务系统：<input  type="text" id="bsysname" />
      管理员：<input type="text" id="bsysadmin" /></p>
     <p> 配置：</p>
      <textarea id="config" rows="30" cols="120"></textarea><br />
      <input type="button" id="modify" value="修改">
      <input type="button" id="back" value="返回">
  </fieldset>
</form>
</body>
<script type="text/javascript">
var bearerToken = "DEBUG userid= 2000284696,realm=lc.service.esp.nd";

$(function(){
	//getToken();
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
})


function showResultList(data){
	var resultTbody = $("#resultTbody");
	resultTbody.empty();
	$.each(data.items,function(i,service){
		if(service.identifier == getUrlParam("identifier")){
			$("#bsysname").val(service.bsysname);
			$("#bsysadmin").val(service.bsysadmin);
			$("#config").val(JSON.stringify(service.bsysivcconfig));
		}
	})
}


$("#modify").click(function(){
	var bsysname = $("#bsysname").val();
	var bsysadmin = $("#bsysadmin").val();
	var config = $("#config").val();
	var json = "{\"bsysname\":\"" + bsysname + "\",\"bsysadmin\":\"" + bsysadmin + "\",\"bsysivcconfig\":" + config + "}";
	$.ajax({
		url:"../v0.6/3dbsys/servicekey/"+getUrlParam("identifier"),
		headers:{"Authorization":bearerToken},
		data:json,
		async:true,
		type:'PUT',
		contentType:"application/json",
		dataType:"json",
	}).done(function(msg){
		location.href = "authorizationConfig.jsp";
	}).fail(function(jqXHR, textStatus){
		alert( "Request failed: " + textStatus );
	});
});

$("#back").click(function(){
	location.href = "authorizationConfig.jsp";
});

//获取url中的参数
function getUrlParam(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
    var r = window.location.search.substr(1).match(reg);  //匹配目标参数
    if (r != null) return unescape(r[2]); return null; //返回参数值
}

function getToken(){
	$.ajax({
		url:"https://aqapi.101.com/v0.93/bearer_tokens",
		data:"{\"login_name\":\"waf_loginer\",\"password\":\"80fba977d063a6f7262a8a9c95f61140\"}",
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