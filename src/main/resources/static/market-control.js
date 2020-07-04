let target = 'FB';

$(function () {
    // Remove button click
    $(document).on(
        'click',
        '[data-role="dynamic-fields"] > .form-inline [data-role="remove"]',
        function (e) {
            e.preventDefault();
            let traderRow = $(this).closest('.form-inline');
            traderRow.remove();
        }
    );
    // Add button click
    $(document).on(
        'click',
        '[data-role="dynamic-fields"] > .form-inline [data-role="add"]',
        function (e) {
            e.preventDefault();
            let container = $('#trader-records');
            let traderRow = container.children().filter('.form-inline:last-child');
            activateTrader(traderRow);
            let new_field_group = traderRow.clone();
            disableChildren(container);
            container.append(new_field_group);
        }
    );

    let bidsRowFunc = function (item) {
        return '<tr><td>'
            + item.quantity + '</td><td>'
            + item.price
            + '</td></tr>';
    }
    getAndUpdateTableData('bidsTable', "/api/v1/gateway/data/order?symbol=" + target + "&type=BUY", bidsRowFunc, target);

    let offersRowFunc = function (item) {
        return '<tr><td>'
            + item.price + '</td><td>'
            + item.quantity
            + '</td></tr>';
    }
    getAndUpdateTableData('offersTable', "/api/v1/gateway/data/order?symbol=" + target + "&type=SELL", offersRowFunc, target);

    let tradesRowFunc = function (item) {
        return '<tr><td>'
            + item.date + '</td><td>'
            + item.symbol + '</td><td>'
            + item.type + '</td><td>'
            + item.status + '</td><td>'
            + item.quantity + '</td><td>'
            + item.price
            + '</td></tr>';
    }

    getAndUpdateTableData('tradesTable', "/api/v1/gateway/data/trade", tradesRowFunc);

    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        target = $(e.target).text() // activated tab
        getAndUpdateTableData('offersTable', "/api/v1/gateway/data/order?symbol=" + target + "&type=SELL", offersRowFunc, target);
        getAndUpdateTableData('bidsTable', "/api/v1/gateway/data/order?symbol=" + target + "&type=BUY", bidsRowFunc, target);
    });


    getAndUpdateExchangeStats();

});

function expandRangeToken(value) {
    let result = value;
    if (value.includes('-')) {
        result = randomIntFromInterval(parseInt(value.split('-')[0]), parseInt(value.split('-')[1]));
    }

    return result;
}

function randomIntFromInterval(min, max) {
    return (min + Math.random() * (max - min + 1)).toFixed(4);
}


function getAndUpdateExchangeStats() {
    $.ajax({
        type: "GET",
        url: "/api/v1/engine/stats",
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        statusCode: {
            200: function (response) {
                $('#engine-stats').html('<b>Total orders:</b> ' + response['totalOrders'] + '<br> <b>Total trades:</b> ' + response['totalTrades'] + '<br> <b>Incoming throughput: </b> ' + response['ordersPerSecond'] + ' (orders/s) <br><b>Matching throughput:</b> ' + response['tradesPerSecond'] + ' (trades/s)');

                setTimeout(getAndUpdateExchangeStats, 1000);
            }
        },
    });
}


function getAndUpdateTableData(table, url, rowFunction, currentTarget, reverse) {
    $.ajax({
        type: "GET",
        url: url,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        statusCode: {
            200: function (response) {
                if (currentTarget == null || currentTarget === target) {
                    $(function () {
                        let rows = '<tbody>';
                        $.each(reverse ? response.reverse() : response, function (i, item) {
                            let tr = rowFunction(item);
                            rows += tr;
                        });
                        rows += '</tbody>';
                        $("#" + table + " tbody").replaceWith(rows);
                    });

                    setTimeout(getAndUpdateTableData, 1000, table, url, rowFunction, currentTarget);
                }
            }
        },
    });
}


function sendOrderRequest(symbol, type, size, price, interval, row, startTime, count) {
    if (row.index() === -1) {
        return;
    }
    const order = {
        symbol: symbol,
        type: type,
        quantity: expandRangeToken(size),
        price: expandRangeToken(price)
    };

    $.ajax({
        type: "POST",
        url: "/api/v1/gateway/trading/order",
        data: JSON.stringify(order),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        statusCode: {

            201: function () {
                ++count;
                row.find('#traderState').show();
                let state = '~ ' + Math.round(100000 * count / (Date.now() - startTime) / 100) + ' requests/s (total: ' + count + ')';
                row.find('#traderState').text(state);
                setTimeout(sendOrderRequest, expandRangeToken(interval), symbol, type, size, price, interval, row, startTime, count);
            },
            404: function (response) {
                console.log(response);
            },
        }
    });
}

function activateTrader(row) {
    let size = row.find('#size').val();
    let price = row.find('#price').val();
    let symbol = row.find('#symbol').val();
    let type = row.find('#orderType').val().toUpperCase();
    let interval = row.find('#interval').val();

    console.log('Trader: ' + symbol + ' ' + type + ' ' + size + ' ' + price + ' ' + interval);
    setTimeout(sendOrderRequest, expandRangeToken(interval), symbol, type, size, price, interval, row, Date.now(), 0);
}

function disableChildren(obj) {
    if (typeof obj.find === "function") {
        obj.find('*').each(function (i, val) {
            disableChildren(val);
            if (!val.disabled && !(val instanceof HTMLButtonElement)) {
                val.disabled = true;
            }
        });
    }
}
