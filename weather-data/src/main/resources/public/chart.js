function drawGraph(locID)
{
    var url = records + locID;
    svg.selectAll('*').remove(); // reset the svg
    d3.json(url,

        function (data) {
            // Add X axis --> it is a date format
            var x = d3.scaleTime()
                .domain(d3.extent(data, function (d) {
                    return d3.timeParse("%Y-%m-%dT%H:%M")(d.date);
                }))
                .range([0, width]);
            svg.append("g")
                .attr("transform", "translate(0," + height + ")")
                .call(d3.axisBottom(x));
            // Add Y axis
            var y = d3.scaleLinear()
                .domain(d3.extent(data, function (d) {
                    return d.temperature;
                }))
                .range([height, 0]);
            svg.append("g")
                .call(d3.axisLeft(y));
            // Add the line
            svg.append("path")
                .datum(data)
                .attr("fill", "none")
                .attr("stroke", "#69b3a2")
                .attr("stroke-width", 1.5)
                .attr("d", d3.line()
                    .curve(d3.curveBasis)
                    .x(function (d) {
                        return x(d3.timeParse("%Y-%m-%dT%H:%M")(d.date))
                    })
                    .y(function (d) {
                        return y(d.temperature)
                    })
                );

            // create a tooltip
            var Tooltip = d3.select("#my_dataviz")
                .append("div")
                .style("opacity", 0)
                .style("position", "absolute")
                .attr("class", "tooltip")
                .style("background-color", "white")
                .style("border", "solid")
                .style("border-width", "1px")
                .style("border-radius", "5px")
                .style("padding", "5px");

            // Three functions that change the tooltip when user hover / move / leave a cell
            var mouseover = function (d) {
                Tooltip
                    .style("opacity", 1)
            };
            var mousemove = function (d) {
                return Tooltip
                    .html("Wind speed: " + d.windSpeed.toFixed(2))
                    .style("left", event.clientX + "px")
                    .style("top", event.clientY + "px");
            };
            var mouseleave = function (d) {
                Tooltip
                    .style("opacity", 0)
            };
            // Add the points
            svg
                .append("g")
                .selectAll("dot")
                .data(data)
                .enter()
                .append("circle")
                .attr("class", "myCircle")
                .attr("cx", function (d) {
                    return x(d3.timeParse("%Y-%m-%dT%H:%M")(d.date))
                })
                .attr("cy", function (d) {
                    return y(d.temperature)
                })
                .attr("r", 5)
                .attr("stroke", "#69b3a2")
                .attr("stroke-width", 1)
                .attr("fill", "white")
                .on("mouseover", mouseover)
                .on("mousemove", mousemove)
                .on("mouseleave", mouseleave);
        });
}