const createChart = data => {
    Chart.defaults.global.defaultFontColor = '#141A46';
    const myChart = new Chart('measurement-chart', {
        type: 'line',
        data: data,
        options: {
            maintainAspectRatio: false,
            scales: {
                xAxes: [{
                    type: 'time',
                    time: {
                        distribution: 'series',
                        displayFormats: {
                            hour: 'HH',
                            minute: 'HH:mm'
                        }
                    }
                }],
                yAxes: [{
                    ticks: {
                        min: 10,
                        stepSize: 1,
                        max: 30}
                }]
            },
            legend: {
                labels: {
                    fontColor: '#141A46'
                }
            }
        }
    });
};

window.onload = () => {
    const limit = new URLSearchParams(window.location.search).get('limit') || 30;

    fetch('chart-data?limit=' + limit)
        .then(response => response.json())
        .then(data => {
            createChart(data);
        });
};
