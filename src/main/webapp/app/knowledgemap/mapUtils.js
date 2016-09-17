/**
 *
 */
function getStartData() {
    if ($(".path_end_div #path_end").val() == "") {
        $("#hidden_div #endId").val("");
    }
    $(".text_start_div > p").remove();
    $.getJSON(neo4jServcerPath + "/knowledgemap/knowledges/actions/queryNode",
        {
            isQueryStart: "true",
            endId: $("#hidden_div #endId").val()
        },
        function (data) {
            $.each(data.nodes, function (n, value) {
                var string = "<p>" + value.title + "</p>";
                var ele = $(string).addClass("text_div_default").click(function () {
                    $(".path_start_div #path_start").val(value.title);
                    $("#hidden_div #startId").val(value.identifier);
                    ele.addClass("text_div_clicked");
//						 getEndData();
                });
                $(".text_start_div").append(ele);
            });
        }
    );
}

function getEndData() {
    if ($(".path_start_div #path_start").val() == "") {
        $("#hidden_div #startId").val("");
    }
    $(".text_end_div > p").remove();
    $.getJSON(neo4jServcerPath + "knowledgemap/knowledges/actions/queryNode",
        {
            isQueryStart: "false",
            startId: $("#hidden_div #startId").val()
        },
        function (data) {
            $.each(data.nodes, function (n, value) {
                var string = "<p>" + value.title + "</p>"
                var ele = $(string).addClass("text_div_default").click(function () {
                    $(".path_end_div #path_end").val(value.title);
                    $("#hidden_div #endId").val(value.identifier);
                    ele.addClass("text_div_clicked");
//						 getStartData();
                });
                $(".text_end_div").append(ele);
            });
        }
    );
}

function searchSourceForNode() {
    if ($("#sidebar_right .search .type").val() == "" || $("#sidebar_right .search .type").val() == null) {
        alert("请选择一个节点！");
        return;
    }
    $("#sidebar_right .content_div .content_ol > li").remove();
    $.getJSON(neo4jServcerPath + "/knowledgemap/knowledges/resource/actions/search",
        {
            resourceType: $("#sidebar_right .search .type").val(),
            kid: $("#sidebar_right .search .indentifier").val()
        },
        function (data) {
            $.each(data.items, function (n, value) {
                var string = "<li>" + value.title + "</li>";
                var ele = $(string).val(value);
                ele.val(n + 1);
                ele.addClass("right_context_li_default");
                var s = value.description;
                var string2;
                if (s == "" || s == null) {
                    string2 = "<div><p>" + "无描述信息" + "</p></div>"
                } else {
                    string2 = "<div><p>" + s + "</p></div>"
                }
                var ele2 = $(string2).addClass("float_description");
                ele.click(function (e) {
                    var X = $('#mainContent #sidebar_right').offset().left;
                    ele2.css("left", X - 200 - 4);
                    ele2.css("top", e.pageY - 35);
                    $("body").append(ele2);
                    ele.removeClass("right_context_li_default");
                    ele.addClass("right_context_li_click");
                });

                ele.mouseout(function () {
                    ele2.remove();
                    ele.removeClass("right_context_li_click")
                    ele.addClass("right_context_li_default");
                });
                $("#sidebar_right .content_div .content_ol").append(ele);
            });
        }
    );
}

function drawMapButton() {
//	$("#hidden_div #maxDepth").val(1);
//	$("#hidden_div #currentDepth").val(1);
    drawmap();
//	test();
}

function createElementForOption(root, values, strings) {
    for (var i = 0; values.length; i++) {
        var str = "<option>" + strings[i] + "</option>";
        var ele = $(str).val(values[1]);
        root.append(ele);
    }
}

