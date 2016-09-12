<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.github.opengrabeso.stravalas.*" %>

<html>

<%
  String authToken = (String) session.getAttribute("authToken");
  String actId = request.getParameter("activityId");
  Main.ActivityEvents laps = Main.getEventsFrom(authToken, actId);
%>


<head>
  <meta charset=utf-8 />
  <title>Strava Split And Lap</title>

  <meta name='viewport' content='initial-scale=1,maximum-scale=1,user-scalable=no' />
  <script src='https://api.tiles.mapbox.com/mapbox-gl-js/v0.23.0/mapbox-gl.js'></script>
  <link href='https://api.tiles.mapbox.com/mapbox-gl-js/v0.23.0/mapbox-gl.css' rel='stylesheet' />

  <style>
    .activityTable {
      border: 0;
      border-collapse: collapse;
    }
    td, th {
      border: 1px solid black;
    }
    .cellNoBorder {
      border: 0;
    }

    #map {
      height: 300px;
      width: 800px;
    }
  </style>

  <script type="text/javascript">
    var id = "<%= actId %>";
    var authToken = "<%= authToken %>";
    var events = [
      <%for (EditableEvent t : laps.editableEvents()) {%> [<%= t %>], <% } %>
    ];

    /**
     * @param {String} id
     * @param event
     * @return {String}
     */
    function splitLink(id, event) {
      var time = event[1];
      var splitWithEvents =
              '  <input type="hidden" name="id" value="' + id + '"/>' +
              '  <input type="hidden" name="auth_token" value="' + authToken + '"/>' +
              '  <input type="hidden" name="operation" value="split"/>' +
              '  <input type="hidden" name="time" value="' + time + '"/>' +
              '  <input type="submit" value="Download"/>';

      var nextSplit = null;
      events.forEach( function(e) {
        splitWithEvents = splitWithEvents + '<input type="hidden" name="events" value="' + e[0] + '"/>';
        if (e[0].lastIndexOf("split", 0) == 0 && e[1] > time && nextSplit == null) {
          nextSplit = e;
        }
      });

      if (nextSplit == null) nextSplit = events[events.length-1];

      var description = "";
      if (nextSplit) {
        var km = (nextSplit[2] - event[2])/1000;
        var duration = nextSplit[1] - event[1];
        var paceSecKm = km > 0 ? duration / km : 0;
        var paceMinKm = paceSecKm / 60;
        var speedKmH = duration > 0 ? km * 3600 / duration : 0;
        description = km.toFixed(2) + " km / " + paceMinKm.toFixed(2) + " min/km / " + speedKmH.toFixed(1) + " km/h";
      }
      return '<form action="download" method="post">' + splitWithEvents + description + '</form>';

    }

    function initEvents() {
      events.forEach(function(e){
        if (e[0].lastIndexOf("split",0) == 0) {
          addEvent(e);
        }
      });
    }

    function addEvent(e) {
      var tableLink = document.getElementById("link" + e[1]);
      tableLink.innerHTML = splitLink(id, e);
    }

    /** @param {String} time */
    function removeEvent(time) {
      var tableLink = document.getElementById("link" + time);
      tableLink.innerHTML = "";
    }

    /**
     * @param {Element} item
     * @param {String} newValue
     * */
    function changeEvent(item, newValue) {
      var itemTime = item.id;
      events.forEach(function(e) {
        if (e[1] == itemTime) e[0] = newValue;
      });

      events.forEach(function(e) {
        if (e[1] == itemTime && e[0].lastIndexOf("split", 0) === 0){
          addEvent(e);
        } else {
          removeEvent(itemTime);
        }
      });

      // without changing the active event first it is often not updated at all, no idea why
      events.forEach(function (e) {
        if (e[0].lastIndexOf("split", 0) === 0) {
          addEvent(e);
        }
      });
    }
  </script>


</head>

<body>

<a href="<%= laps.id().link()%>"><%= laps.id().name()%></a>

<form action ="download" method="post">
  <input type="hidden" name="id" value="<%= laps.id().id()%>"/>
  <input type="hidden" name="auth_token" value="<%= authToken%>"/>
  <input type="hidden" name="operation" value="copy"/>
  <input type="submit" value="Backup original activity"/>
</form>

  <table class="activityTable">
    <tr>
      <th>Event</th>
      <th>Time</th>
      <th>km</th>
      <th>Sport</th>
      <th>Action</th>
    </tr>
    <%
      EditableEvent[] ees = laps.editableEvents();
      String lastSport = "";
      int lastTime = -1;
      for (int i = 0; i < laps.events().length; i ++ ) {
        Event t = laps.events()[i];
        EditableEvent ee = ees[i];
        String action = ee.action();
        String sport = ee.sport().equals(lastSport) ? "" : ee.sport();
        lastSport = ee.sport();
    %>
    <tr>
      <td><%= t.description()%>
      </td>
      <td><%= Main.displaySeconds(t.stamp().time()) %></td>
      <td><%= Main.displayDistance(t.stamp().dist()) %></td>
      <td><%= sport %></td>
      <td> <%
          EventKind[] types = t.listTypes();
          if (types.length != 1 && lastTime != t.stamp().time()) {
            lastTime = t.stamp().time();
        %>
        <select id="<%=t.stamp().time()%>" name="events" onchange="changeEvent(this, this.options[this.selectedIndex].value)">
            <%
              for (EventKind et : types) {
            %> <option value="<%= et.id()%>"<%= action.equals(et.id()) ? "selected" : ""%>><%= et.display()%></option>
            <% }
          %></select>
        <% } else { %>
          <%= Events.typeToDisplay(types, types[0].id())%>
          <input type="hidden" name = "events" value = "<%= t.defaultEvent() %>"/> <%
        } %>
      </td>
      <td class="cellNoBorder" id="link<%=t.stamp().time()%>"></td>
    </tr>
    <% } %>
  </table>

  <div id='map'></div>

  <script type="text/javascript">initEvents()</script>

  <script>
    mapboxgl.accessToken = 'pk.eyJ1Ijoib3NwYW5lbCIsImEiOiJjaXQwMXBqaGcwMDZ4MnpvM21ibzl2aGM5In0.1DeBqAQXvxLPajeeSK4jQQ';
    var map = new mapboxgl.Map({
      container: 'map',
      style: 'mapbox://styles/mapbox/streets-v9'
    });
  </script>

</body>
</html>
