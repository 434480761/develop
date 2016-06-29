var firstcreate = true;
function drawmap() {
    if ($(".path_end_div #path_end").val() == "" && $(".path_start_div #path_start").val() == "") {
        alert("至少有一个起点或终点的UUID");
        return;
    }
    if ($(".path_end_div #path_end").val() == "") {
        $("#hidden_div #endId").val("");
    }

    if ($(".path_start_div #path_start").val() == "") {
        $("#hidden_div #startId").val("");
    }

    $.getJSON(neo4jServcerPath + "/knowledgemap/knowledges/actions/findPaths",
        {
            startId: $("#hidden_div #startId").val(),
            endId: $("#hidden_div #endId").val(),
            maxDepth: $("#hidden_div #maxDepth").val(),
            minDepth: $("#hidden_div #minDepth").val()
        },
        function (data) {
            var nodes = createNodes(data, null);
            var edges = createRelations(data, null);
            checkData(nodes, edges);
            console.log(nodes);
            console.log(edges);
            createMap(nodes, edges, 160, 2, 23, firstcreate);
            firstcreate = false;
        }
    );
}

function getMapAttrs(linkDistance) {
    var mapAttrs = {};
    //基本属性
    mapAttrs.width = 1000;
    mapAttrs.height = 800;
    mapAttrs.linkDistance = linkDistance;
    mapAttrs.charge = -1500;

    //箭头属性
    mapAttrs.mark = {};
    mapAttrs.mark.lenght = 90;
    return mapAttrs;
}

var force;

function updataMap(width, height) {
    d3.select("#content svg")
        .attr("width", width)
        .attr("height", height);

    force.size([width, height]);
}

function createMap(nodes, edges, linkDistance, lineWidth, nodeRadius, firstcreate) {

    //更具节点数的不同获取不同的参数
    var mapAttrs = getMapAttrs(linkDistance);
    var isFirst = true;

    $("svg").remove();

    var svg = d3.select("#content")
        .append("svg")
        .attr("width", mapAttrs.width)
        .attr("height", mapAttrs.height);
//	svg.
    force = d3.layout.force()
        .nodes(nodes) // 指定节点数组
        .links(edges) // 指定连线数组
        .size([mapAttrs.width, mapAttrs.height]) // 指定范围
        .linkDistance(linkDistance) // 指定连线长度
        .charge([mapAttrs.charge]); // 相互之间的作用力
    //加快响应速度
    for (var i = 0; i < nodes.length; i++) {
        var n = nodes[i];
        var j = i % 5;
        n.px = 400 + 10 * j;
        n.py = 500 + 10 * j;
        n.x = 400 + 10 * j;
        n.y = 500 + 10 * j;
    }

    //在绘制完图前先隐藏
    $("svg").hide();

    force.start(); // 开始作用

    svg.append("svg:defs")
        .selectAll("marker")
        .data(["suit"])
        .enter()
        .append("svg:marker")
        .attr("id", String)
        .attr("viewBox", "0 0 10 10")
        .attr("refX", 25)
        .attr("refY", 6.2)
        .style("fill", "#666")
        .attr("markerWidth", 8)
        .attr("markerHeight", 8)
        .attr("orient", "auto")
        .append("svg:path")
        .attr("d", "M2,2 L10,6 L2,10 L6,6 L2,2");

    // 添加连线
    var svg_edges = addLines(force, svg, edges, lineWidth);

    var edges_text = svg.selectAll(".linetext")
        .data(edges)
        .enter()
        .append("text")
        .attr("class", "linetext")
        .text(function (d) {
            return "";
        });

    var svg_nodes = addCircle(force, svg, nodes, nodeRadius);

    var svg_texts = addTextForNode(force, svg, nodes);

    force.on("tick", function () { // 对于每一个时间间隔
        //等到力图基本稳定后再显示
        if (isFirst) {
            if (force.alpha() <= 0.08) {
                isFirst = false;
                $("svg").show();
            }
            return;
        }
        // 更新连线坐标
        svg_edges.attr("x1", function (d) {
            return d.source.x;
        }).attr("y1", function (d) {
            return d.source.y;
        }).attr("x2", function (d) {
            return d.target.x;
        }).attr("y2", function (d) {
            return d.target.y;
        });

        edges_text.attr("x", function (d) {
            return (d.source.x + d.target.x) / 2;
        });
        edges_text.attr("y", function (d) {
            return (d.source.y + d.target.y) / 2;
        });

        // 更新节点坐标
        svg_nodes.attr("cx", function (d) {
            return d.x;
        }).attr("cy", function (d) {
            return d.y;
        });
        // 更新文字坐标
        svg_texts.attr("x", function (d) {
            return d.x;
        }).attr("y", function (d) {
            return d.y;
        });

    });

    var oldEdges = new Array();
    var oldNodes;

    var mousedisup = 1;
    var mousedisdown = -1;
    var mousespeed = 6;
    $('#mainContent #content').unbind("mousewheel");
    $('#mainContent #content').bind('mousewheel',
        function (event, delta, deltaX, deltaY) {
            if ($("#hidden_div #startId").val() != "" && $("#hidden_div #endId").val() != "") {
                return;
            }
            if (delta > 0) {
                console.log(delta);
                mousedisup++;
                if (mousedisup % mousespeed == 0) {
                    var currentDepth = $("#hidden_div #maxDepth").val();
                    currentDepth = parseInt(currentDepth);
                    currentDepth = currentDepth + 1;
                    if (currentDepth > 20) {
                        currentDepth = 20;
                    }
                    updataDepthData(currentDepth);
                    console.log(currentDepth + "," + $("#hidden_div #minDepth").val() + "," + $("#hidden_div #maxDepth").val());
                    $.getJSON(neo4jServcerPath + "/knowledgemap/knowledges/actions/findPaths",
                        {
                            startId: $("#hidden_div #startId").val(),
                            endId: $("#hidden_div #endId").val(),
                            maxDepth: $("#hidden_div #maxDepth").val(),
                            minDepth: $("#hidden_div #minDepth").val()
                        },
                        function (data) {

                            //---
                            for (var i = 0; i < edges.length; i++) {
                                var e = $.extend(true, {}, edges[i]);
                                oldEdges.push(e);
                            }

                            //获取数据
                            var newNodes = createNodes(data, nodes);
                            var newEdeges = createRelations(data, nodes);

                            if (newNodes.length == nodes.length) {
                                var currentDepth = $("#hidden_div #maxDepth").val();
                                currentDepth = parseInt(currentDepth);
                                currentDepth = currentDepth - 1;
                                if (currentDepth <= 1) {
                                    currentDepth = 1;
                                }
                                updataDepthData(currentDepth);
                                return;
                            }

                            edges = newEdeges;

                            //添加新的节点
                            for (var i = 0; i < newNodes.length; i++) {
                                var have = false;
                                n = newNodes[i];
                                for (var j = 0; j < nodes.length; j++) {
                                    on = nodes[j];
                                    if (on.indentifier == n.indentifier) {
                                        have = true;
                                        break;
                                    }
                                }
                                //降低图像的抖动
                                if (!have) {
                                    n.px = 400 + 10 * i;
                                    n.py = 500 + 10 * i;
                                    n.x = 400 + 10 * i;
                                    n.y = 500 + 10 * i;
                                    nodes.push(n);
                                }
                            }

                            //对数据进行转换
                            force.nodes(nodes);
                            force.links(edges);

                            //添加线
                            addLines(force, svg, edges, lineWidth);

                            //添加节点
                            addCircle(force, svg, nodes, nodeRadius);

                            // 添加节点文字
                            addTextForNode(force, svg, nodes);

                            //删除多余的节点的文字
                            exit3 = svg.selectAll(".nodetext").data(nodes);
                            exit3.exit().remove();

                            //删除多余的节点
                            exit2 = svg.selectAll("circle").data(nodes);
                            exit2.exit().remove();

                            //删除多余的线
                            exit = svg.selectAll("line").data(edges);
                            exit.exit().remove();

                            //对节点、关系、文字进行排序
                            sortElement();

                            //tick
                            svg_texts = svg.selectAll(".nodetext");
                            svg_edges = svg.selectAll("line");
                            svg_nodes = svg.selectAll("circle");

                            force.start();
                        }
                    );
                }
            } else {
                mousedisdown--;
                if (mousedisdown % mousespeed == 0) {

                    var currentDepth = $("#hidden_div #maxDepth").val();
                    currentDepth = parseInt(currentDepth);
                    currentDepth = currentDepth - 1;
                    if (currentDepth < 1) {
                        currentDepth = 1;
                    }
                    updataDepthData(currentDepth);
                    console.log(currentDepth + "," + $("#hidden_div #minDepth").val() + "," + $("#hidden_div #maxDepth").val());
                    $.getJSON(neo4jServcerPath + "/knowledgemap/knowledges/actions/findPaths",
                        {
                            startId: $("#hidden_div #startId").val(),
                            endId: $("#hidden_div #endId").val(),
                            maxDepth: $("#hidden_div #maxDepth").val(),
                            minDepth: $("#hidden_div #minDepth").val()
                        },
                        function (data) {
                            //创建数据
                            var newNodes = createNodes(data, null);
                            var newEdeges = createRelations(data, nodes);

                            //删除多余的节点
                            for (var i = 0; i < nodes.length;) {
                                var have = false;
                                on = nodes[i];
                                for (var j = 0; j < newNodes.length; j++) {
                                    n = newNodes[j];
                                    if (on.indentifier == n.indentifier) {
                                        have = true;
                                        break;
                                    }
                                }
                                if (!have) {
                                    nodes.splice(i, 1);
                                } else {
                                    i++;
                                }
                            }

                            //删除多余的关系
                            while (true) {
                                if (edges.length <= newEdeges.length) {
                                    break;
                                }
                                for (var i = 0; i < edges.length; i++) {
                                    var have = false;
                                    var oedg = edges[i];
                                    for (var j = 0; j < newEdeges.length; j++) {
                                        var nedg = newEdeges[j];
                                        if ((oedg.source.indentifier == nedg.source) && (oedg.target.indentifier == nedg.target)) {
                                            have = true;
                                            break;
                                        }
                                    }
                                    if (!have) {
                                        edges.splice(i, 1);
                                        break;
                                    }
                                }
                            }

                            //转换数据
                            force.nodes(nodes);
                            force.links(edges);

                            //添加节点
                            addCircle(force, svg, nodes, nodeRadius);

                            //添加线
                            addLines(force, svg, edges, lineWidth);

                            //添加节点文字
                            addTextForNode(force, svg, nodes);
                            svg_texts = svg.selectAll(".nodetext");

                            svg_edges = svg.selectAll("line").data(edges);
                            svg_nodes = svg.selectAll("circle").data(nodes);

                            force.start();

                            //删除多余的节点的文字
                            exit3 = svg.selectAll(".nodetext").data(nodes);
                            exit3.exit().remove();

                            //删除多余的节点
                            exit2 = svg.selectAll("circle").data(nodes);
                            exit2.exit().remove();

                            //删除多余的线
                            exit = svg.selectAll("line").data(edges);
                            exit.exit().remove();
                        }
                    );
                }
            }
        });

}

//检查数据是否正确
function checkData(nodes, edges) {
    console.log("检查json数据是否正确");
}

//排序顺序line、lineText、circle、circleText
function sortElement() {
    var lines = $("line");
    var fnodes = $("circle").first();
    fnodes.before(lines)

    var llines = $("line").last();
    var nodes = $("circle");
    llines.after(nodes);

    var lnodes = $("circle").last();
    var texts = $("text");
    lnodes.after(texts);
}

//添加节点文字
function addTextForNode(force, svg, nodes) {
    return svg.selectAll(".nodetext")
        .data(nodes)
        .enter()
        .append("text")
        .attr("class", "nodetext")
        .attr("dx", -20)
        .style("fill", "#999999")
        .attr("dy", -25).text(
            function (d) {
                return d.name;
            }
        );
}

//添加线
function addLines(force, svg, edges, lineWidth) {
    return svg.selectAll("line")
        .data(edges)
        .enter()
        .append("line")
        .style("stroke", "#888")
        .attr("x", 25)
        .attr("y", 6.2)
        .attr("marker-end", function (d) {
            return "url(#suit)";
        })
        .style("stroke-width", lineWidth)
}

//添加节点
function addCircle(force, svg, nodes, nodeRadius) {
    return svg.selectAll("circle")
        .data(nodes)
        .enter()
        .append("circle")
        .on("click", function (d, i) {
            d3.selectAll("circle").data(nodes).style("fill", function (d, i) {
                return d.color
            });
            d3.select(this).style("fill", "#4876FF");
            getKnowledgeType(d);
        })
        .attr("r", nodeRadius)
        .style("fill", function (d, i) {
            return d.color;
        })
        .call(force.drag);
}

/**
 * 利用传递过来的json数据转换成自定义的D3数据<br>
 * data 从服务器获取的json数据
 * */
function createNodes(data, oldData) {

    var nodeArray = new Array();

    var nodes = data.nodes;

    var color = d3.scale.category20();

    $.each(nodes, function (n, value) {
        //D3使用的node数据模型
        var node = {name: "", color: "", indentifier: "", type: "", properties: {}};

        node.name = value.title;
        node.indentifier = value.identifier;
        if (checkIsStartNode(value)) {
            node.name = value.title;
            node.color = "#7CFC00";
        } else if (checkIsEndNode(value)) {
            node.name = value.title;
            node.color = "#FF0000";
        } else {
            node.name = value.title;
//			node.color = color(n); 
            node.color = "#63B8FF";
        }
        nodeArray.push(node);
    });

    return nodeArray;
}

/**
 * 利用传递过来的json数据转换成自定义的D3数据<br>
 * data 从服务器获取的json数据
 * */
function createRelations(data, oldData) {
    //获取关系原文件
    var relations = data.relations;

    //转换成D3需要的数据
    var edges = new Array();
    $.each(relations, function (n, value) {
        var obj = {relation: "", source: "-1", target: "-1", type: "resolved"};
        obj.source = value.source;
        obj.target = value.target;

        obj.type = value.type;
        obj.relation = value.relation;
        edges.push(obj);
    });

    return edges;
}

function checkIsStartNode(node) {
    if (node.identifier == $("#hidden_div #startId").val()) {
        return true;
    } else {
        return false;
    }
}

function checkIsEndNode(node) {
    if (node.identifier == $("#hidden_div #endId").val()) {
        return true;
    } else {
        return false;
    }
}

function getKnowledgeType(d) {
    var indentifier = d.indentifier;
    var name = d.name;
    //ajax请求
    var typeList = new Array();
    typeList.push("coursewares");
    typeList.push("questions");

    //删除原数据
    $("#sidebar_right .search .type > option").remove();

    //创建节点
    $.each(typeList, function (n, value) {
        var string = "<option>" + value + "</option>";
        ele = $(string).val(value);
        $("#sidebar_right .search .type").append(ele);
    });

    //设置UUID
    $("#sidebar_right .search .indentifier").val(indentifier);
    $("#sidebar_right .search .name").val(name);

}

function updataDepthData(currentDepth) {
    $("#hidden_div #maxDepth").val(currentDepth);
    $("#hidden_div #currentDepth").val(currentDepth);
    $("#search_depth_div .search_depth").val(currentDepth);
}
