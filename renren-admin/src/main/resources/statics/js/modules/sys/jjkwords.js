var buttonArr=[];
$(function () {
    var map = {};
    var rows={"id":""};
    map.首营送审="firstSend('"+rows.id+"')";
    map.修改后审批="secondSend('"+rows.id+"')";
    map.查看 ="vm.getInfo('"+rows.id+"')";
    map.通过 ="vm.accpect('"+rows.id+"',true)";
    map.不通过="vm.accpect('"+rows.id+"',false)";

    $("#jqGrid").jqGrid({
        url: baseURL + 'sys/jjkwords/list',
        datatype: "json",
        colModel: [			
			{ label: '序号', name: 'id', index: 'id', width: 50, key: true },
			{ label: '姓名', name: 'name', index: 'name', width: 80 },
			{ label: '年龄', name: 'age', index: 'age', width: 80 },
			{ label: '创建人', name: 'createby', index: 'createBy', width: 80 },
			{ label: '状态', name: 'typeStr', index: 'type', width: 80 },
			// { label: '节点', name: 'nodeName', index: 'type', width: 80 },
            { label: '操作', name: 'typeStr', index: 'state', width: 150, edittype:"button", formatter: cmgStateFormat},
            { label: '操作', name: 'buttonIds', index: 'buttonIds', width: 50, edittype:"button",hidden:true},
            { label: '操作', name: 'assign', index: 'assign', width: 50, edittype:"button",hidden:true},
            { label: '操作', name: 'roleList', index: 'roleList', width: 50, edittype:"button",hidden:true}
        ],
		viewrecords: true,
        height: 385,
        rowNum: 10,
		rowList : [10,30,50],
        rownumbers: true, 
        rownumWidth: 25, 
        autowidth:true,
        multiselect: true,
        pager: "#jqGridPager",
        jsonReader : {
            root: "page.list",
            page: "page.currPage",
            total: "page.totalPage",
            records: "page.totalCount"
        },
        prmNames : {
            page:"page", 
            rows:"limit", 
            order: "order"
        },
        gridComplete:function(){
            console.info(buttonArr);
            $.each($(".btn"), function(i, n){
                var isHava = buttonArr.toString().indexOf((n.text?n.text:n.value).trim());
                if(isHava===-1){
                    $(n).hide();
                }
            });
        	//隐藏grid底部滚动条
        	$("#jqGrid").closest(".ui-jqgrid-bdiv").css({ "overflow-x" : "hidden" }); 
        },

    });

    function cmgStateFormat(cellValue,grid, rows, state) {
        if(!rows["buttonIds"]) return null;
        if($.parseJSON(rows["buttonIds"])!=null && $.parseJSON(rows["buttonIds"]).buttons!=null){
            var buttons = $.parseJSON(rows["buttonIds"]).buttons;

            if(rows["roleList"].toString().indexOf(rows["assign"])!==-1){
                buttonArr.push(buttons["noun"]);
                console.info(buttons["noun"]);
            }

            var buttonList = "";
            var seeName = "查看";


            if(buttons.line!=null){
                for (var i = 0; i < buttons.line.length; i++) {
                    if(rows["typeStr"]==="未提交" && buttons.line[i]==="修改送审"){
                        continue;
                    }
                    if(rows["typeStr"]!=="未提交" && buttons.line[i]==="首营送审"){
                        continue;
                    }
                    if(map[buttons.line[i]]&&rows["roleList"].toString().indexOf(rows["assign"])!==-1){

                      buttonList+=  "<button class='btn btn-primary'  onclick="+map[buttons.line[i]].replace("''",rows["id"])+" style='margin-left: 10px'>"+buttons.line[i]+"</button> "
                    }else {
                        buttonList+=  "<button class='btn btn-primary'  onclick="+map["查看"].replace("''",rows["id"])+" style='margin-left: 10px'>"+seeName+"</button> "
                        break;
                    }
                }
            }
            return  buttonList;
        }
        return "";

    }





});
//首迎送审
function firstSend(id) {
    alert("firstSend");
    $.ajax({
        type: "POST",
        url: baseURL + "sys/jjkwords/firstSend?id="+id,
        success: function(r){
            if(r.code === 0){
                layer.msg("操作成功", {icon: 1});
                $("#jqGrid").trigger("reloadGrid");
            }else{
                layer.alert(r.msg);
            }
        }
    });
}
//修改后审批
function secondSend(id) {
    alert("secondSend");
    $.ajax({
        type: "POST",
        url: baseURL + "sys/jjkwords/secondSend?id="+id,
        success: function(r){

            if(r.code === 0){
                layer.msg("操作成功", {icon: 1});
                $("#jqGrid").trigger("reloadGrid");
            }else{
                layer.alert(r.msg);
            }
        }
    });
}
var vm = new Vue({
	el:'#rrapp',
	data:{
		showList: true,
		title: null,
		jjkWords: {}
	},
	methods: {
		query: function () {
			vm.reload();
		},
		add: function(){
			vm.showList = false;
			vm.title = "新增";
			vm.jjkWords = {};
		},
		update: function (event) {
			var id = getSelectedRow();
			if(id == null){
				return ;
			}
			vm.showList = false;
            vm.title = "修改";

            vm.getInfo(id)
		},
		saveOrUpdate: function (event) {
		    $('#btnSaveOrUpdate').button('loading').delay(1000).queue(function() {
                var url = vm.jjkWords.id == null ? "sys/jjkwords/save" : "sys/jjkwords/update";
                $.ajax({
                    type: "POST",
                    url: baseURL + url,
                    contentType: "application/json",
                    data: JSON.stringify(vm.jjkWords),
                    success: function(r){
                        if(r.code === 0){
                             layer.msg("操作成功", {icon: 1});
                             vm.reload();
                             $('#btnSaveOrUpdate').button('reset');
                             $('#btnSaveOrUpdate').dequeue();
                        }else{
                            layer.alert(r.msg);
                            $('#btnSaveOrUpdate').button('reset');
                            $('#btnSaveOrUpdate').dequeue();
                        }
                    }
                });
			});
		},
		del: function (event) {
			var ids = getSelectedRows();
			if(ids == null){
				return ;
			}
			var lock = false;
            layer.confirm('确定要删除选中的记录？', {
                btn: ['确定','取消'] //按钮
            }, function(){
               if(!lock) {
                    lock = true;
		            $.ajax({
                        type: "POST",
                        url: baseURL + "sys/jjkwords/delete",
                        contentType: "application/json",
                        data: JSON.stringify(ids),
                        success: function(r){
                            if(r.code == 0){
                                layer.msg("操作成功", {icon: 1});
                                $("#jqGrid").trigger("reloadGrid");
                            }else{
                                layer.alert(r.msg);
                            }
                        }
				    });
			    }
             }, function(){
             });
		},
		getInfo: function(id){
			$.get(baseURL + "sys/jjkwords/info/"+id, function(r){
                vm.jjkWords = r.jjkWords;
            });
            if(id == null){
                return ;
            }
            vm.showList = false;
            vm.title = "查看";
		},
		reload: function (event) {
			vm.showList = true;
			var page = $("#jqGrid").jqGrid('getGridParam','page');
			$("#jqGrid").jqGrid('setGridParam',{ 
                page:page
            }).trigger("reloadGrid");

		},
        accpect: function(id,fool){
            $.post(baseURL + "sys/jjkwords/accpect/"+id+"?fool="+fool, function(r){
                vm.jjkWords = r.jjkWords;
                vm.reload();
            });

		}



	}
});