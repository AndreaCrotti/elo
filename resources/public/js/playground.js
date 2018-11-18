var config = window.config;
const default_domain = [1300, 1700];

// TODO: use also the game range to filter out the graph generated
const league_id = config['league_id'];
var rankings_url = "/api/rankings-json?league_id=" + league_id;

const render_graph = async () => {
    const response = await fetch(rankings_url);
    const json = await response.json();

    const jsonSpec = {
        "$schema": "https://vega.github.io/schema/vega-lite/v3.0.0-rc6.json",
        "description": "Players rankings",
        "data": {
            "values": json,
        },
        "mark": {
            "type": "line",
            "point": {"tooltip": {"content": "data"}}
        },
        "encoding": {
            "x": {
                "field": "Time",
                "type": "temporal"
            },
            "y": {
                "field": "Ranking",
                "type": "quantitative",
                "scale": {"domain": default_domain}
            },
            "color": {
                "field": "Player",
                "type": "nominal" 
            }
        }
    }

    vegaEmbed('#vega-visualization', jsonSpec);
}

render_graph();
