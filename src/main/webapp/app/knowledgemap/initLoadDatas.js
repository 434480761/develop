$(document).ready(function(){
	
	$(window).ready(function(){
		winWidth = window.innerWidth ;
		updetaWindow(winWidth);
	});
	
	$(window).resize(function(){
		winWidth = window.innerWidth ;
		updetaWindow(winWidth);
	});
	
	var isfirst = true ;
	if(isfirst){
		getStartData() ;
		$(".path_start_div #path_start").val("");
		getEndData();
		isfirst = false ;
	}
	
	//点击起点事件
	$(".path_start_div #path_start").click(function(){
		if($(".path_end_div #path_end").val()==""){
		}
			$(".text_start_div").show();
		});
	
	//点击终点事件
	$(".path_end_div #path_end").click(function(){
		if($(".path_start_div #path_start").val()==""){
			$("#hidden_div #startId").val("");
		}
		$(".text_end_div").show();
	});
	
	//鼠标移开，发送请求
	$("#start_div").mouseleave(function(){
		$(".text_start_div").hide();
		 getEndData();
	});
	
	//鼠标移开，发送请求
	$("#end_div").mouseleave(function(){
		  $(".text_end_div").hide();
		  getStartData();
	});
	

	$("#search_depth_div .search_depth").change(function(ele){
		var depth = $("#search_depth_div .search_depth").val();
		$("#hidden_div #maxDepth").val(depth);
		$("#hidden_div #currentDepth").val(depth);
	});
	
	//监听提交按钮
	$("#sidebar_right .search .submit").click(function(){
		searchSourceForNode();
	});
});

function updetaWindow(width){
	var leftwidth = 0 ;
	var rightwidth = 350 ;
	var contentwidth ;
	
	
	if(width>1600){
		width = 1600 ;
	}else if(width<1000){
		width = 1000 ;
	}
	
	contentwidth = width - leftwidth - rightwidth - 30;
	
	$("#container").css("width",width);
	$("#sidebar_lift").css("width" , leftwidth) ;
	$("#sidebar_right").css("width" ,rightwidth)
	$("#content").css("width" , contentwidth);
	
	updataMap(contentwidth , 800) ;
	
}
