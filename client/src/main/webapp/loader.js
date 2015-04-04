function waitForPolymer(waitMaxAttempts, waitPause, success, error) {

    var busyIndicator = document.createElement("img");
    busyIndicator.src = "data:image/gif;base64,R0lGODlhFAAZAIABAAAAAP///yH/C05FVFNDQVBFMi4wAwEAAAAh+QQFCgABACwAAAAAFAAZAAACG4yPqcDt3wycT9mLs968+w+G4kiW5omm6sqSBQAh+QQFCgABACwBAAUAEgACAAACBYSPqcsFACH5BAUKAAEALAEACQASAAIAAAIFhI+pywUAIfkEBQoAAQAsAQANABIAAgAAAgWEj6nLBQAh+QQFCgABACwBABEAEgACAAACBYSPqcsFACH5BAkKAAEALAEAAQASABYAAAIXjI+py+0Po5y02ouz3rz7BoTiSJbmWQAAIfkECQoAAQAsAAAAABQAGQAAAieMj6nL7Q+jA7TaWw3eV/rIhYAmbt/ZlGagYuibtB0rZ/Bd2zl1wwUAIfkECQoAAQAsAAAAABQAGQAAAiWMj6nL7Q+jnLRaB7LeXJsOctd4hCbwnSBJqmvgduwYi3DtzVUBACH5BAkKAAEALAAAAAAUABkAAAIijI+py+0Po5y02ouzZqD7D3pGSILbVabAqJKn1bpBHL5UAQAh+QQJCgABACwAAAAAFAAZAAACHoyPqcvtD6OctNqLs968UwCG4hga5Dl6H8qaLKpKBQAh+QQJCgABACwAAAAAFAAZAAACG4yPqcvtD6OctNqLs968+w9OwEiWJmmcqhlCBQAh+QQFCgABACwAAAAAFAAZAAACFIyPqcvtD6OctNqLs968+w+G4ogUADs=";
    busyIndicator.style.position = "fixed";
    busyIndicator.style.top = "1em";
    busyIndicator.style.right = "1em";

    document.body.appendChild(busyIndicator);

    window.addEventListener("polymer-ready", function (e) {
        document.body.removeChild(busyIndicator);
    });

    waitForPolymerLoop(0, waitMaxAttempts, waitPause, success, error);
}

function waitForPolymerLoop(attempt, max, pause, success, error) {
    if (window.PolymerGestures) {  // Who had this bright window.Polymer stub idea in webcomponents.js?
        success.call()
    } else if (attempt <= max) {
        setTimeout(function () {
            waitForPolymerLoop(attempt++, max, pause, success, error)
        }, pause);
    } else {
        error.call()
    }
}
