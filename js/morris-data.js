$(function() {

    Morris.Area({
        element: 'morris-area-chart',
        xkey: 'period',
        ykeys: ['iphone', 'ipad', 'itouch'],
        labels: ['iPhone', 'iPad', 'iPod Touch'],
        pointSize: 2,
        hideHover: 'auto',
        resize: false
    });

    Morris.Donut({
        element: 'morris-donut-chart',
        data: [{
            label: "Sensor 1",
            value: 10
        }, {
            label: "Sensor 2",
            value: 20
        }, {
            label: "Sensor 3",
            value: 30
        }
        , {
            label: "Sensor 4",
            value: 40
        }, {
            label: "Sensor 5",
            value: 50
        }, {
            label: "Sensor 6",
            value: 60
        }],
        resize: true
    });

    Morris.Bar({
        element: 'morris-bar-chart',
        data: [{
            y: 'Monday',
            a: 100,
            b: 90,
            c: 10
        }, {
            y: 'Tuesday',
            a: 75,
            b: 65,
            c: 10
        }, {
            y: 'Wednesday',
            a: 50,
            b: 40,
            c: 10
        }, {
            y: 'Thursday',
            a: 75,
            b: 65,
            c: 10
        }, {
            y: 'Friday',
            a: 50,
            b: 40,
            c: 10
        }, {
            y: 'Saturday',
            a: 75,
            b: 65,
            c: 10
        }, {
            y: 'Sunday',
            a: 100,
            b: 90,
            c: 10
        }],
        xkey: 'y',
        ykeys: ['a', 'b', 'c'],
        labels: ['Sensor 1', 'Sensor 2', 'Sensor 3'],
        hideHover: 'auto',
        resize: true
    });


});
