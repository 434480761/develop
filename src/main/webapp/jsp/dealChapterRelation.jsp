<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>导入章节关系表格</title>

<script src="../js/jquery-1.8.3.min.js"></script>
<style type="text/css">
table{ border-collapse:collapse; width:90%; border:1px #cccccc solid}
th{ border:1px solid #cccccc;padding: 10px;}
td{ border:1px solid #cccccc;padding: 5px;}
</style>

</head>
<body>
	<form id="fileForm" action="../jsp/checkData" enctype="multipart/form-data" method="post">
		<input id="excelFile" type="file" name="material">
		<input type="submit" value="导入">
	</form>
	<div id="checkDataResultDiv" style="display:none;width:100%;margin: 10px 0px;overflow-y:auto;">
		<table style="width: 100%;">
			<thead>
			<tr>
				<th width="25%">基准教材章节</th>
				<th width="25%">参照章节1</th>
				<th width="25%">参照章节2</th>
				<th>校验结果</th>
			</tr>
			</thead>
			<tbody id="contentTbody"></tbody>
		</table>
	</div>
	<div id="checkDataResultOperationDiv" style="text-align: right;">
		<input id="nextBtn" type="button" value="下一步" onclick="createRelation()"/>
		<span id="messageSpan" style="color: red;">数据有误，请修改后重新上传</span>
	</div>
	<div id="dealDataResultDiv" style="display:none;width:100%;margin: 10px 0px;overflow-y:auto;">
		<table style="width: 100%;">
			<thead>
			<tr>
				<th width="25%">基准教材章节</th>
				<th width="25%">参照章节1</th>
				<th width="25%">参照章节2</th>
				<th>关系创建结果</th>
			</tr>
			</thead>
			<tbody id="resultTbody"></tbody>
		</table>

	</div>
	<div id="dealDataResultOperationDiv" style="display:none;text-align: right;">
		<input id="finishBtn" type="button" value="完成" onclick="window.location.href='../jsp/toImportJsp';"/>
	</div>
</body>
<script type="text/javascript">
	var fileName = "";
	//展示校验结果
	var showResultList = function(lists){
		$("#fileForm").hide();
		$("#checkDataResultDiv").hide();
		$("#checkDataResultOperationDiv").hide();
		$("#dealDataResultDiv").show();
		$("#dealDataResultOperationDiv").show();
		var resultTbody = $("#resultTbody");
		resultTbody.empty();
		$.each(lists,function(i,list){
			var $tr = $("<tr>").appendTo(resultTbody);
			var $td0 = $("<td>").appendTo($tr);
			var $td1 = $("<td>").appendTo($tr);
			var $td2 = $("<td>").appendTo($tr);
			var $td3 = $("<td>").appendTo($tr);
			$td0.html(list[0]);
			$td1.html(list[1]);
			$td2.html(list[2]);
			var result = list[5];
			$td3.html(result);
			if(result.indexOf("失败") > 0){
				$td3.css("color","red");
			}else if(result.indexOf("存在") > 0){
				$td3.css("color","blue");
			}
			if(i % 2 == 1){
				$tr.css("background-color","#eee");
			}
		})
	}
	
	//展示关系创建结果列表
	var showContentList = function(lists){
		var contentTbody = $("#contentTbody");
		contentTbody.empty();
		$.each(lists,function(i,list){
			var $tr = $("<tr>").appendTo(contentTbody);
			var $td0 = $("<td>").appendTo($tr);
			var $td1 = $("<td>").appendTo($tr);
			var $td2 = $("<td>").appendTo($tr);
			var $td3 = $("<td>").appendTo($tr);
			$td0.html(list[0]);
			$td1.html(list[1]);
			$td2.html(list[2]);
			var result = list[3];
			$td3.html(result);
			if(result != "通过"){
				$td3.css("color","red");
			}
			if(i % 2 == 1){
				$tr.css("background-color","#eee");
			}
		})
	}
	
	//创建资源关系
	var createRelation = function(){
		$("#nextBtn").attr("disabled",true);
		$.ajax({
			url:"../jsp/dealData",
			data:{"fileName":fileName},
			async:true,
			type:'POST',
			dataType:"json",
			success:function(data){
				$("#nextBtn").attr("disabled",false);
				if(data != null){
					if(data.resultCode == "0"){
						alert(data.resultMessage);
					}else{
						var contentList = data.contentList;
						showResultList(contentList);
					}
				}
			},
			error:function(){
				$("#nextBtn").attr("disabled",false);
			}
		});
	}
	
	//初始化页面
	$(function(){
		var height = $(window).height()-100;
		$("#checkDataResultDiv").css("height",height+"px");
		$("#dealDataResultDiv").css("height",height+"px");
		var resultMap = ${resultMap};
		if(resultMap == null){
			$("#checkDataResultDiv").hide();
			$("#checkDataResultOperationDiv").hide();
		}else{
			var resultCode = resultMap.resultCode;
			var resultMessage = resultMap.resultMessage;
			var contentList = resultMap.contentList;
			if(resultCode == "0"){
				$("#nextBtn").hide();
				$("#messageSpan").show();
				if(resultMessage != ""){
					alert(resultMessage);
				}				
			}else{
				$("#nextBtn").show();
				$("#messageSpan").hide();
				fileName = resultMap.fileName;
			}
			if(contentList != null){
				$("#checkDataResultDiv").show();
				$("#dealDataResultDiv").hide();
				$("#checkDataResultOperationDiv").show();
				$("#dealDataResultOperationDiv").hide();
				//展示列表
				showContentList(contentList);
			}
		}
	})
</script>
</html>