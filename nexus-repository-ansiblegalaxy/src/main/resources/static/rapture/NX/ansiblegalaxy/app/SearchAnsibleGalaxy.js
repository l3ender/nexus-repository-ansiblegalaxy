Ext.define('NX.ansiblegalaxy.app.SearchAnsibleGalaxy', {
    extend: 'NX.app.Controller',
    requires: [
        'NX.I18n'
    ],

    /**
     * @override
     */
    init: function () {
        var me = this,
            search = me.getController('NX.coreui.controller.Search');

        search.registerFilter({
            id: 'ansiblegalaxy',
            name: 'ansiblegalaxy',
            text: NX.I18n.get('SearchAnsibleGalaxy_Text'),
            description: NX.I18n.get('SearchAnsibleGalaxy_Description'),
            readOnly: true,
            criterias: [
                {id: 'format', value: 'ansiblegalaxy', hidden: true},
                {id: 'name.raw'},
                {id: 'version'}
            ]
        }, me);
    }
});