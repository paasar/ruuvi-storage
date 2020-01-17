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
                    id: 'temperature',
                    ticks: {
                        min: 10,
                        stepSize: 1,
                        max: 27},
                    position: 'right'
                },
                {
                    id: 'humidity',
                    ticks: {
                        min: 3,
                        stepSize: 3,
                        max: 54},
                    position: 'left'
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
        .then(json => {
            if (json.data) {
                createChart(json.data);
            } else {
                console.log('Could not fetch chart data. Error message:', json.error);
            }
        });
};

