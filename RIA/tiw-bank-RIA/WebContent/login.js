/**
 * Login management
 */


 /**
  * Checks if the values in the form are correct
  */
 function checkForm() {
	 var pwd = document.getElementById("pwd").value;
	 var repeatpwd = document.getElementById("repeatpwd").value;
	 if (!(pwd===repeatpwd)) {
		 document.getElementById("regmessage").textContent = "Mismatching between fields password and repeat password";
		 return false;
	 } else {
     var email = document.getElementById("email").value;
     var re = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,6})+$/
     if (re.test(email)) {
       return true;
     } else {
       document.getElementById("regmessage").textContent = "Email is not correct";
       return false;
     }

	 }

 }

(function() { // avoid variables ending up in the global scope

  document.getElementById("registerform").hidden = true;
  document.getElementById("registerLink").addEventListener('click', (e) => {
	  document.getElementById("registerform").hidden = false;

  });
  document.getElementById("registerbutton").addEventListener('click', (e) => {
	  var form = e.target.closest("form");
	    if (form.checkValidity() && checkForm()) {
	      makeCall("POST", 'Register', e.target.closest("form"),
	        function(req) {
	          if (req.readyState == XMLHttpRequest.DONE) {
	            var message = req.responseText;
	            switch (req.status) {
	              case 200:
	            	document.getElementById("regmessage").textContent = message;
	                break;
	              case 400: // bad request
	                document.getElementById("regmessage").textContent = message;
	                break;
	              case 500: // server error
	            	document.getElementById("regmessage").textContent = message;
	                break;
	            }
	          }
	        }
	      );
	    } else {
	    	 form.reportValidity();
	    }
  });

  document.getElementById("loginbutton").addEventListener('click', (e) => {
    var form = e.target.closest("form");
    if (form.checkValidity()) {
      makeCall("POST", 'CheckLogin', e.target.closest("form"),
        function(req) {
          if (req.readyState == XMLHttpRequest.DONE) {
            var message = req.responseText;
            switch (req.status) {
              case 200:
            	sessionStorage.setItem('username', message);
              window.location.href = "homepage.html";
                break;
              case 400: // bad request
                document.getElementById("errorlogmessage").textContent = message;
                break;
              case 401: // unauthorized
                  document.getElementById("errorlogmessage").textContent = message;
                  break;
              case 500: // server error
            	document.getElementById("errorlogmessage").textContent = message;
                break;
            }
          }
        }
      );
    } else {
    	 form.reportValidity();
    }
  });

})();
