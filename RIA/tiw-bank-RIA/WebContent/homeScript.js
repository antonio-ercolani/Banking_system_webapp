(function() {

    function PageOrchestrator() {
        var alertContainer = document.getElementById("id_alert");
        var errorMsgContainer = document.getElementById("errorMsg");

        this.start = function() {

            personalMessage = new PersonalMessage(sessionStorage.getItem('username'),
                document.getElementById("id_username"));

            personalMessage.show();

            bankAccountList = new BankAccountList(
                document.getElementById("accountList"),
                document.getElementById("id_listcontainerbody"),
                alertContainer);


            form = new TransferForm(
                    document.getElementById("form"),
                    document.getElementById("addressBookBtn"),
                    errorMsgContainer,
                    document.getElementById("myInput"),
                    document.getElementById("id_summary"),
                    document.getElementById("id_summarybody")
                );
                form.registerEvents(this);


            accountInfo = new AccountInfo(
                form,
                document.getElementById("id_transfersListcontainer"),
                document.getElementById("id_transfersListcontainerbody"),
                alertContainer);


            bankAccountList.show(undefined, false);
            form.autocomplete();
            document.getElementById("id_summary").style.visibility = "hidden";
            document.getElementById("addressBookBtn").style.visibility = "hidden";

            document.querySelector("a[href='Logout']").addEventListener('click', () => {
            window.sessionStorage.removeItem('user');
            window.location.href = "index.html";
          })
      };



        	this.refresh = function(bankID) {
            alertContainer.textContent = "";
            errorMsgContainer.textContent = "";

            accountInfo.reset();
            bankAccountList.reset();

            bankAccountList.show(bankID, true);

            form.reset();
          };


    }

    var personalMessage, bankAccountList, accountInfo, form,
      pageOrchestrator = new PageOrchestrator();

      let contacts = [];

      window.addEventListener("load", () => {
        if (sessionStorage.getItem("username") == null) {
          window.location.href = "index.html";
        } else {
          pageOrchestrator.start();
        }
      }, false);

      function PersonalMessage(message, msgContainer) {

          this.show = function () {
              msgContainer.textContent = message;
          }

      }


      function BankAccountList(listContainer, listContainerBody, alert) {
        this.alert = alert;
        this.listContainerBody = listContainerBody;
        this.listcontainer = listContainer;

        this.reset = function() {
          this.listcontainer.style.visibility = "hidden";
        }

        this.show = function(bank_id, showSummary) {
          var self = this;
          makeCall("GET", "GetAccounts", null,
            function(req) {
              if (req.readyState == 4) {
                var message = req.responseText;
                if (req.status == 200) {
                  var accounts = JSON.parse(req.responseText);
                  if (accounts.length == 0) {
                    self.alert.textContent = "You have no accounts";
                    return;
                  }
                  self.update(accounts);
                  self.autoclick(bank_id, showSummary);
                }
              } else {
                self.alert.textContent = message;
              }
            }
          );
        };

        this.autoclick = function(bank_id, showSummary) {
          var e = new Event("click");
          var selector = "a[bank_id='" + bank_id + "']";
          var anchorToClick =
            (bank_id) ? document.querySelector(selector) : document.querySelector("a[bank_id]");
          if (anchorToClick) anchorToClick.dispatchEvent(e);
          if (showSummary) {
            document.getElementById("id_summary").style.visibility = "visible";
          }
      };


        this.update = function(accounts) {
          var row, idCell, balanceCell, anchor;
          this.listContainerBody.innerHTML = "";
          var self = this;
          accounts.forEach(function(account) {
            row = document.createElement("tr");
            idCell = document.createElement("td");
            anchor = document.createElement("a");
            idCell.appendChild(anchor);
            linkText = document.createTextNode(account.id);
            anchor.appendChild(linkText);
            anchor.setAttribute('bank_id', account.id);
            anchor.addEventListener("click", (e) => {
            accountInfo.show(e.target.getAttribute("bank_id"));
            document.getElementById("id_summary").style.visibility = "hidden";
            }
              , false);
            anchor.href = "#";

            row.appendChild(idCell);
            balanceCell = document.createElement("td");
            balanceCell.textContent = account.balance;
            row.appendChild(balanceCell);

            self.listContainerBody.appendChild(row);
          });
        this.listcontainer.style.visibility = "visible";

      }
    }



  		function AccountInfo(formContainer, listContainer, listContainerBody, alert) {

  			this.alert = alert;
  			this.listcontainer = listContainer;
  			this.listContainerBody = listContainerBody;



            this.reset = function() {
              this.listcontainer.style.visibility = "hidden";
            };

            this.show = function(bankId) {
              formContainer.setBankID(bankId);
              var self = this;
              var form = document.createElement("form");
              var input = document.createElement("input");
              input.name = "bank_id";
              input.value = bankId;
              form.appendChild(input);
              makeCall("POST", "CheckOwnership", form,
                function(req) {
                  if (req.readyState == 4) {
                    var message = req.responseText;
                    if (req.status == 200) {
                      var transfers = JSON.parse(req.responseText);
                      self.updateTransfers(transfers);
                    }

                }
              }
            );
        };


        this.updateTransfers = function(transfers) {
  			var row, idCell, sourceCell, destinationCell, amountCell, dateCell;
  			this.listContainerBody.innerHTML = "";
  			var self = this;
  			transfers.forEach(function(transfer) { // self visible here, not this
  				row = document.createElement("tr");
  				idCell = document.createElement("td");
  				idCell.textContent = transfer.id;
  				row.appendChild(idCell);

  				sourceCell = document.createElement("td");
  				sourceCell.textContent = transfer.source;
  				row.appendChild(sourceCell);

  				destinationCell = document.createElement("td");
  				destinationCell.textContent = transfer.destination;
  				row.appendChild(destinationCell);

  				amountCell = document.createElement("td");
  				amountCell.textContent = transfer.amount;
  				row.appendChild(amountCell);

  				dateCell = document.createElement("td");
  				dateCell.textContent = transfer.date;
  				row.appendChild(dateCell);

  				self.listContainerBody.appendChild(row);
  			});

  			this.listcontainer.style.visibility = "visible";

        };

      }



      function TransferForm(formContainer, addressBookBtn, alert, destField, summary, summarybody) {
                this.formContainer = formContainer;
                this.alert = alert;
                this.addressBookContainer = addressBookBtn;
                this.summary = summary;
                this.summarybody = summarybody;

                var destID = 0;

                var bankID = 0;

                var contacts = [];

                this.setBankID = function (bankid) {
                    bankID = bankid;
                };

                this.autocomplete = getAutocompleteList;

                function getAutocompleteList() {
                    makeCall("GET", "GetAddressBook", null,
                        function(req) {
                            if (req.readyState == 4) {
                                var message = req.responseText;
                                if (req.status == 200) {
                                    contacts = JSON.parse(message);
                                }
                            } else {
                                self.alert.textContent = message;
                            }
                        });
                }

                this.registerEvents = function(orchestrator) {

                    formContainer.addEventListener('submit', (event) => {
                        event.preventDefault();
                        sendTransferForm(this.formContainer);
                    });

                    addressBookBtn.addEventListener('click', function () {
                        sendAddContactForm();

                    });

                    this.reset = function () {
                        this.formContainer.reset();
                    };


                    function checkFormValidity(form) {
                        error = false;
                        if(isNaN(form.userDest.value))
                            error = true;
                        if(isNaN(form.destination.value))
                            error = true;
                        if(isNaN(form.amount.value))
                            error = true;

                        return error;
                    }

                    function summaryDetails(summary) {
                        summarybody.innerHTML = "";
                        let row1 = document.createElement("tr");
                        let row2 = document.createElement("tr");
                        let row3 = document.createElement("tr");
                        let name = document.createElement("th");
                        name.textContent = "name";
                        let surname = document.createElement("th");
                        surname.textContent = "surname";
                        let balance = document.createElement("th");
                        balance.textContent = "balance";
                        row1.appendChild(name);
                        row2.appendChild(surname);
                        row3.appendChild(balance);

                        let sourceCell1 = document.createElement("td");
                        sourceCell1.textContent = summary.userID.name;
                        row1.appendChild(sourceCell1);

                        let destinationCell1 = document.createElement("td");
                        destinationCell1.textContent = summary.destID.name;
                        row1.appendChild(destinationCell1);

                        let sourceCell2 = document.createElement("td");
                        sourceCell2.textContent = summary.userID.surname;
                        row2.appendChild(sourceCell2);

                        let destinationCell2 = document.createElement("td");
                        destinationCell2.textContent = summary.destID.surname;
                        row2.appendChild(destinationCell2);

                        let sourceCell3 = document.createElement("td");
                        sourceCell3.textContent = summary.userID.balance;
                        row3.appendChild(sourceCell3);

                        let destinationCell3 = document.createElement("td");
                        destinationCell3.textContent = summary.destID.balance;
                        row3.appendChild(destinationCell3);

                        summarybody.appendChild(row1);
                        summarybody.appendChild(row2);
                        summarybody.appendChild(row3);

                    }

                    function checkContact() {
                        var form = document.createElement("form");
                        var input = document.createElement("input");
                        input.name = "contact";
                        input.value = destID;
                        form.appendChild(input);
                        makeCall("POST", "CheckContact", form,
                            function(req) {
                                if (req.readyState == 4) {
                                    if (req.status == 200) {
                                        addressBookBtn.style.visibility = "visible";
                                    }
                                    if(req.status == 400)
                                    {
                                        addressBookBtn.style.visibility = "hidden";
                                    }

                                } else {
                                    self.alert.textContent = "Errore";
                                }
                            }
                        )
                    }

                    function sendTransferForm(form) {
                        if(checkFormValidity(form)) {
                            alert.textContent = "Error: wrong format";

                            form.reset();
                            return;
                        } else if(form.amount.value < 0) {
                          alert.textContent = "NEGATIVE AMOUNT";
                          form.reset();
                          return;
                        }
                        form.bankID.value = bankID;
                        destID = form.userDest.value;

                        makeCall("POST", "CreateTransfer", form,
                            function(req) {
                                if (req.readyState == 4) {
                                    var message = req.responseText;
                                    if (req.status == 200) {
                                        orchestrator.refresh(bankID);

                                        summary.style.visibility = "visible";
                                        checkContact();
                                        summaryDetails(JSON.parse(message));
                                    }
                                    if(req.status == 400) {
                                        switch (Number(message)) {
                                            case 1:
                                                alert.textContent = "NEGATIVE AMOUNT";
                                                break;
                                            case 2:
                                                alert.textContent = "INSUFFICINET FUNDS";
                                                break;
                                            case 3:
                                                alert.textContent = "USER DOES NOT MATCH DEST ACCOUNT";
                                                break;
                                            case 4:
                                                alert.textContent = "DEST ACCOUNT DOESN NOT EXISTS";
                                                break;
                                            case 5:
                                                alert.textContent = "DEST USER DOES NOT EXISTS";
                                                break;
                                            case 6:
                                                alert.textContent = "YOU CANNOT TRANSFER MONEY TO YOURSELF";
                                                break;
                                            default:
                                                alert.textContent = "AN ERROR OCCURRED"
                                        }

                                        summary.style.visibility = "hidden";
                                    }
                                }
                                else {
                                    self.alert.textContent = message;
                                }
                            });
                    }


                    function sendAddContactForm() {
                        var form = document.createElement("form");
                        var input = document.createElement("input");
                        input.name = "contact";
                        input.value = destID;
                        form.appendChild(input);
                        makeCall("POST", "AddContact", form,
                            function(req) {
                                if (req.readyState == 4) {

                                    if (req.status == 200) {
                                        alert.textContent = destID + " salvato in rubrica";
                                        getAutocompleteList();
                                        addressBookBtn.style.visibility = "hidden";

                                    }
                                } else {
                                    self.alert.textContent = "errore";
                                }
                            });
                    }

                    /* ------ AUTOCOMPLETE STUFF ------- */

                    /**
                     * autocomplete function by W3C School
                     */
                    (function autocomplete(inp) {
                        /*the autocomplete function takes two arguments,
                        the text field element and an array of possible autocompleted values:*/
                        var currentFocus;
                        /*execute a function when someone writes in the text field:*/
                        inp.addEventListener("input", function(e) {
                            var a, b, i, val = this.value;
                            /*close any already open lists of autocompleted values*/
                            closeAllLists();
                            if (!val) { return false;}
                            currentFocus = -1;
                            /*create a DIV element that will contain the items (values):*/
                            a = document.createElement("DIV");
                            a.setAttribute("id", this.id + "autocomplete-list");
                            a.setAttribute("class", "autocomplete-items");
                            /*append the DIV element as a child of the autocomplete container:*/
                            this.parentNode.appendChild(a);
                            /*for each item in the array...*/
                            for (i = 0; i < contacts.length; i++) {
                                /*check if the item starts with the same letters as the text field value:*/
                                if (contacts[i].substr(0, val.length).toUpperCase() == val.toUpperCase()) {
                                    /*create a DIV element for each matching element:*/
                                    b = document.createElement("DIV");
                                    /*make the matching letters bold:*/
                                    b.innerHTML = "<strong>" + contacts[i].substr(0, val.length) + "</strong>";
                                    b.innerHTML += contacts[i].substr(val.length);
                                    /*insert a input field that will hold the current contactsay item's value:*/
                                    b.innerHTML += "<input type='hidden' value='" + contacts[i] + "'>";
                                    /*execute a function when someone clicks on the item value (DIV element):*/
                                    b.addEventListener("click", function(e) {
                                        /*insert the value for the autocomplete text field:*/
                                        inp.value = this.getElementsByTagName("input")[0].value;
                                        /*close the list of autocompleted values,
                                        (or any other open lists of autocompleted values:*/
                                        closeAllLists();
                                    });
                                    a.appendChild(b);
                                }
                            }
                        });
                        /*execute a function presses a key on the keyboard:*/
                        inp.addEventListener("keydown", function(e) {
                            var x = document.getElementById(this.id + "autocomplete-list");
                            if (x) x = x.getElementsByTagName("div");
                            if (e.keyCode == 40) {
                                /*If the arrow DOWN key is pressed,
                                increase the currentFocus variable:*/
                                currentFocus++;
                                /*and and make the current item more visible:*/
                                addActive(x);
                            } else if (e.keyCode == 38) { //up
                                /*If the arrow UP key is pressed,
                                decrease the currentFocus variable:*/
                                currentFocus--;
                                /*and and make the current item more visible:*/
                                addActive(x);
                            } else if (e.keyCode == 13) {
                                /*If the ENTER key is pressed, prevent the form from being submitted,*/
                                e.preventDefault();
                                if (currentFocus > -1) {
                                    /*and simulate a click on the "active" item:*/
                                    if (x) x[currentFocus].click();
                                }
                            }
                        });
                        function addActive(x) {
                            /*a function to classify an item as "active":*/
                            if (!x) return false;
                            /*start by removing the "active" class on all items:*/
                            removeActive(x);
                            if (currentFocus >= x.length) currentFocus = 0;
                            if (currentFocus < 0) currentFocus = (x.length - 1);
                            /*add class "autocomplete-active":*/
                            x[currentFocus].classList.add("autocomplete-active");
                        }
                        function removeActive(x) {
                            /*a function to remove the "active" class from all autocomplete items:*/
                            for (var i = 0; i < x.length; i++) {
                                x[i].classList.remove("autocomplete-active");
                            }
                        }
                        function closeAllLists(element) {
                            /*close all autocomplete lists in the document,
                            except the one passed as an argument:*/
                            var x = document.getElementsByClassName("autocomplete-items");
                            for (var i = 0; i < x.length; i++) {
                                if (element !== x[i] && element !== inp) {
                                    x[i].parentNode.removeChild(x[i]);
                                }
                            }
                        }
                        /*execute a function when someone clicks in the document:*/
                        document.addEventListener("click", function (e) {
                            closeAllLists(e.target);
                        });

                    })(destField);

                }




            }

})();
