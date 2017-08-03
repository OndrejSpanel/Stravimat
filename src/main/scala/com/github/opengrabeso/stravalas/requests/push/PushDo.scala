package com.github.opengrabeso.stravalas
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
          <span id="done"></span> of <span id="total"></span>
        </p>
        <a href="javascript:update();false">List pending</a>
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
              var totalFiles = $(response).find("total").first().text();
              var doneFiles = $(response).find("done").first().text();
              $("#total").html(totalFiles);
              $("#done").html(doneFiles);
            });
          }
          """)}
        </script>

      </body>
    </html>

  }
}
