
let unreadTexts = 0;

function update() {
  console.log("Updating");

  fetch("https://api.github.com/repos/sgmr29/harbinger/issues/1", { cache: 'no-store' })
    .then(function (response) { return response.json(); })
    .then(function (data) {
      // use the json
      unreadTexts = data.body;
      lastUpdate = Date.parse(data.updated_at);
      minutesAgo = (Date.now() - lastUpdate) / (60 * 1000);
      document.getElementById("lastUpdateSpan").textContent = "Last update was " + Math.round(minutesAgo) + " minutes ago"
      
      if (minutesAgo < 17) {
        document.getElementById("statusSpan").textContent = "You have " + unreadTexts + " unread texts.";
        if (unreadTexts == 0) {
          document.title = "Harbinger";
          document.getElementById("sceneImg").src = "images/zero.jpg";
        } else if (unreadTexts == 1) {
          document.title = "(" + unreadTexts + ") Harbinger";
          document.getElementById("sceneImg").src = "images/one.jpg";
        } else {
          document.title = "(" + unreadTexts + ") Harbinger";
          document.getElementById("sceneImg").src = "images/many.jpg";
        }
      } else {
        document.title = "(?) Harbinger";
        document.getElementById("statusSpan").textContent = "Your phone is not connected.";
        document.getElementById("sceneImg").src = "images/broken.jpg";
      }
    })
    .catch((e) => console.error(e));
}

update();

setInterval(function () {
  update();
}, 120 * 1000); // milliseconds
