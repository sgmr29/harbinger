
let unreadTexts = 0;

function update() {
    fetch("state/unreadTexts.txt")
        .then((res) => res.text())
        .then((text) => {
            unreadTexts = text;
            document.title = "(" + unreadTexts + ") Harbinger";
            document.getElementById("status").textContent = "You have " + unreadTexts + " unread texts";
            if (unreadTexts == 0) {
                document.getElementById("scene").src = "images/zero.jpg";
              } else if (unreadTexts == 1) {
                document.getElementById("scene").src = "images/one.jpg";
              } else {
                document.getElementById("scene").src = "images/many.jpg";
              }
        })
        .catch((e) => console.error(e));
}

update();

setInterval(function () {
    // Your code to run every few seconds
    console.log("This runs every 30 seconds");
    update();
}, 30000); // milliseconds


