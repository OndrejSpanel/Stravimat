package com.github.opengrabeso.stravamat
package requests
package push

import spark.{Request, Response}

object PushDo extends DefineRequest("/push-do") with ActivityRequestHandler {
  override def urlPrefix = "push-"
  def html(req: Request, resp: Response) = {
    val session = req.session
    val auth = session.attribute[Main.StravaAuthResult]("auth")
    // display push progress, once done, let user to process it
    <html>
      <head>
        {headPrefix}<title>Stravamat - uploading Suunto files</title>
        <style>
          tr.activities:nth-child(even) {{background-color: #f2f2f2}}
          tr.activities:hover {{background-color: #f0f0e0}}
        </style>
        <script src="static/ajaxUtils.js"></script>
        <script src="static/jquery-3.2.1.min.js"></script>
      </head>
      <body>
        {bodyHeader(auth)}<h2>Receiving Suunto files ...</h2>
        <p>
          <span id="done">--</span> of <span id="total">--</span>
        </p>
        <div id="uploaded" style="display: none;">
          {
          // TODO: integrate selectActivity functionality here
          }
          <a href="selectActivity">Select activity</a>
        </div>
        <script>
          {xml.Unparsed(
          //language=JavaScript
          """
          function update() {
            $.ajax({
              url: "push-list-pending",
              dataType: "xml",
              cache: false
            }).done( function(response) {
              var totalElem = $(response).find("total");
              var doneElem = $(response).find("done");
              if (/*totalElem && doneElem &&*/ totalElem.length > 0 && doneElem.length > 0) {
                var totalFiles = parseInt(totalElem.first().text());
                var doneFiles = parseInt(doneElem.first().text());
                $("#total").html(totalFiles);
                $("#done").html(doneFiles);
                if (doneFiles < totalFiles) setTimeout(update, 1000);
                else $("#uploaded").show();
              } else setTimeout(update, 1000);
            });
          }
          update();
          """)}
        </script>

      </body>
    </html>

  }
}