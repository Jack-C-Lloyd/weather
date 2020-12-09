const data = [
    ['', 'Tesla', 'Volvo', 'Toyota', 'Ford'],
    ['2019', 10, 11, 12, 13],
    ['2020', 20, 11, 14, 13],
    ['2021', 30, 15, 12, 13]
];
var host = "http://localhost:4567/";
var recordsURL = host + "records";
var locationsURL = host + "locations";
var locationsData;
var recordsData;

d3.json(locationsURL, function (data) {
    locationsData = data;
    d3.json(recordsURL, function (data) {
        recordsData = data.map(d => {
            d.location = locationsData.find(l => {
                return l.locID === d.locID;
            }).name;
            return d;
        });
        makeTable();
    });
});

function makeTable() {
    const container = document.getElementById('example');
    console.log(locationsData);
    const hot = new Handsontable(container, {
        data: recordsData,
        rowHeaders: true,
        colHeaders: ['ID', 'LocID', 'Date', 'Temp', 'Humidity', 'Wind speed', 'Wind direction', 'Location'],
        hiddenColumns: {
            columns: [1],
            indicators: true
        },
        dropdownMenu: true,
        filters: true,
        licenseKey: 'non-commercial-and-evaluation'
    });
}