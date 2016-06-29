neo4jServcerPath = getRootPath();
function getRootPath(){
    var curPath=window.document.location.href;
    var pathName=window.document.location.pathname;
    var pos=curPath.indexOf(pathName);
    var localhostPath=curPath.substring(0,pos);
    var projectName=pathName.substring(0,pathName.substr(1).indexOf('/')+1);
    return(localhostPath+projectName+"/");
}