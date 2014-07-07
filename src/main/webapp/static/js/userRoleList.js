var roleListParams = {
    NO_WEAPON: "没有装备武器",
    NO_ACCESSORY: "没有装备饰品",
    NO_ARMOR: "没有装备防具"
}

var userRoleListLoader = function () {

    CommonUtil.beforeLoad();
    var c = new Command("role", "userRoleList", {});
    var simpleRoleCollection = new Backbone.Collection;
    CommonUtil.doPost(c, function (msg) {

        // 加载数据到collection中
        _.each(msg.data.list, function (userRole, index, list) {
            var simpleRoleModel = new Backbone.Model({
                roleName: userRole.roleName,
                fightForce: userRole.fightForce,
                attack: userRole.attack,
                defence: userRole.defence,
                health: userRole.health,
                level: userRole.level,
                id: userRole.id,
                armor: userRole.armor,
                accessory: userRole.accessory,
                weapon: userRole.weapon,
                armorInfo: userRole.weaponInfo,
                accessoryInfo: userRole.accessoryInfo,
                weaponInfo: userRole.weaponInfo
            });
            simpleRoleCollection.add(simpleRoleModel);

        });
    });

    var RoleListView = Backbone.View.extend({
        el: "#roleList",
        template: _.template($("#roleRowTemplate").html()),
        template2: _.template($("#roleDetailTemplate").html()),


        render: function () {
            for (var m  in this.model) {
                this.eachModel(this.model[m]);
            }
            this.showRoleDetail({data: {role: this.model[0].toJSON()}});
            return this;
        },
        eachModel: function (data) {
            $(this.el).append(this.template(data.toJSON()));
            $("#showRoleDetail" + data.toJSON().id).on("click", {role: data.toJSON()}, this.show2);
        },
        show2: function (event) {
            roleListView.showRoleDetail(event);
        },
        showRoleDetail: function (event) {
            $("#roleDetail").html("");
            var role = event.data.role;
            var t = this.template2({
                roleName: role.roleName,
                fightForce: role.fightForce,
                attack: role.attack,
                defence: role.defence,
                health: role.health,
                level: role.level,
                id: role.id,
                weapon: role.weapon,
                accessory: role.accessory,
                armor: role.armor,
                NO_WEAPON: roleListParams.NO_WEAPON,
                NO_ACCESSORY: roleListParams.NO_ACCESSORY,
                NO_ARMOR: roleListParams.NO_ARMOR

            });
            $("#roleDetail").html(t);
            $("#weapon" + role.id).on("click", {id: role.weapon, urId: role.id, type: "weapon"}, this.nav2EquipDetail);
            $("#accessory" + role.id).on("click", {id: role.accessory, urId: role.id, type: "accessory"}, this.nav2EquipDetail);
            $("#armor" + role.id).on("click", {id: role.armor, urId: role.id, type: "armor"}, this.nav2EquipDetail);

        },
        nav2EquipDetail: function (event) {
            if (event.data.id == 0) {
                CommonUtil.nav2Url("wearEquip.html", {
                    ueId: event.data.id,
                    type: event.data.type,
                    urId: event.data.urId
                });
            } else {
                CommonUtil.nav2Url("equipDetail.html", {
                    ueId: event.data.id,
                    type: event.data.type,
                    urId: event.data.urId
                });
            }

        }
    });

    var roleListView = new RoleListView({model: simpleRoleCollection.models});
    roleListView.render();
}