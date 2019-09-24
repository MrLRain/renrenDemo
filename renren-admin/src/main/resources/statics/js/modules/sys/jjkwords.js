$(function () {
    $("#jqGrid").jqGrid({
        url: baseURL + 'sys/jjkwords/list',
        datatype: "json",
        colModel: [			
			{ label: '序号', name: 'id', index: 'id', width: 50, key: true },
			{ label: '姓名', name: 'name', index: 'name', width: 80 },
			{ label: '年龄', name: 'age', index: 'age', width: 80 },
			{ label: '创建人', name: 'createby', index: 'createBy', width: 80 },
			{ label: '状态', name: 'typeStr', index: 'type', width: 80 },
			{ label: '节点', name: 'nodeName', index: 'type', width: 80 },
            { label: '操作', name: 'typeStr', index: 'state', width: 150, edittype:"button", formatter: cmgStateFormat},
            { label: '操作', name: 'processId', index: 'state', width: 50, edittype:"button",hidden:true}
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

        	//隐藏grid底部滚动条
        	$("#jqGrid").closest(".ui-jqgrid-bdiv").css({ "overflow-x" : "hidden" }); 
        },

    });
    function cmgStateFormat(cellValue,grid, rows, state) {
        var processId=rows["processId"];
        if (cellValue == "未提交") {
            return "<button class='btn btn-primary'  onclick=\"firstSend('"  + processId + "','"+rows.id+"')\" style='margin-left: 10px'>首迎送审</button> "
                +"<button class='btn btn-warning ' onclick=\"secondSend('"  +  processId + "','"+rows.id+"')\">修改后审批</button>";
        }
        else if(cellValue == "失败")
        {
            return "<button class='btn btn-default' onclick=\"secondSend('"  +   rows.id + "','\"+processId+\"')\">修改后审批</button>";
        }else {
            return "<button class='btn btn-default' onclick=\"lookAt('"  +   rows.id + "')\">查看</button>";
        }
    }
});
//首迎送审
function firstSend(processId,id) {
    alert("firstSend");
    $.ajax({
        type: "POST",
        url: baseURL + "sys/jjkwords/firstSend?id="+id+"&processId="+processId,
        success: function(r){
            if(r.code === 0){
                layer.msg("操作成功", {icon: 1});
                $("#jqGrid").trigger("reloadGrid");1
            }else{
                layer.alert(r.msg);
            }
        }
    });
}
//修改后审批
function secondSend(processId,id) {
    alert("secondSend");
    $.ajax({
        type: "POST",
        url: baseURL + "sys/jjkwords/secondSend?id="+id+"&processId="+processId,
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
		},
		reload: function (event) {
			vm.showList = true;
			var page = $("#jqGrid").jqGrid('getGridParam','page');
			$("#jqGrid").jqGrid('setGridParam',{ 
                page:page
            }).trigger("reloadGrid");
		}
	}
});