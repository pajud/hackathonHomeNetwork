$(function() {
    Morris.Bar({
        element: 'morris-bar-chart',
        data: [{
            y: 'Monday',
            a: 100,
            b: 90,
            c: 10,
            d: 20
        }, {
            y: 'Tuesday',
            a: 75,
            b: 65,
            c: 10,
            d: 20
        }, {
            y: 'Wednesday',
            a: 50,
            b: 40,
            c: 10,
            d: 20
        }, {
            y: 'Thursday',
            a: 75,
            b: 65,
            c: 10,
            d: 20
        }, {
            y: 'Friday',
            a: 50,
            b: 40,
            c: 10,
            d: 20
        }, {
            y: 'Saturday',
            a: 75,
            b: 65,
            c: 10,
            d: 20
        }, {
            y: 'Sunday',
            a: 100,
            b: 90,
            c: 10,
            d: 20
        }],
        xkey: 'y',
        ykeys: ['a', 'b', 'c', 'd'],
        labels: ['Sensor 1', 'Sensor 2', 'Sensor 3', 'Sensor 4'],
        barColors: ['#0b62a4', '#7a92a3', '#4da74d', '#afd8f8', '#edc240', '#cb4b4b', '#9440ed'],
        hideHover: 'auto',
        resize: true
    });
});
