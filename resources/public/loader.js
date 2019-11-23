const createChart = data => {
  const myChart = new Chart('measurement-chart', {
    type: 'line',
    data: data,
    options: {
        maintainAspectRatio: false,
        scales: {
            xAxes: [{
                type: 'time',
                time: {
                    distribution: 'series'
                }
            }],
            yAxes: [{
                ticks: {
                    min: 10,
                    stepSize: 1,
                    max: 30}
            }]
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

