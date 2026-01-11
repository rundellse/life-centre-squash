
class Division {
    constructor(divisionNum) {
        this.divisionNum = divisionNum;
        this.players = [];
    }
}

class DivisionUpdate {
    constructor(id, division, redFlag) {
        this.id = id;
        this.division = division;
        this.redFlag = redFlag;
    }
}

let divisionsCount = 0;


document.addEventListener('DOMContentLoaded', function() {
    populateAdminTable();
    configureSeasonButtons();
});


function populateAdminTable() {
    const apiUrl = 'http://localhost:8080/api/players';
    const tableBlock = document.querySelector('#table-block');
    const divisions = [];

    fetch(apiUrl, {
        method: 'GET',
        credentials: 'include'
    })
        .then(response => response.json())
        .then(players => {
            players.forEach(player => {
                let divisionNum = player.division;
                let division = divisions[divisionNum];
                if (typeof division === 'undefined' || division === null) {
                    division = new Division(divisionNum);
                    divisions[divisionNum] = division;
                }
                division.players.push(player);
            });
            return divisions;
        })
        .then(divisions => {
            divisions.forEach(division => {
                const divisionLength = division.players.length;
                const divisionTable = document.createElement('table');
                divisionTable.setAttribute('id', 'playerTable' + division.divisionNum);
                divisionTable.innerHTML =`<thead class="table-top-row"><tr><th colspan="6"></th></tr></thead>`

                for (let i = 0; i < divisionLength; i++) {
                    const player = division.players[i];
                    addPlayerRowToDivisionTable(i, player, divisionTable);
                }
                tableBlock.appendChild(divisionTable);
                divisionsCount++;
            })
        })
        .catch(error => console.error('Error fetching players:', error));
}

function configureSeasonButtons() {
    document.getElementById('save-divisions-button-top').onclick = updateDivisions;
    document.getElementById('save-divisions-button-bottom').onclick = updateDivisions;

    document.getElementById('new-season-button').onclick = newSeason;
    document.getElementById('generate-pdf-button').onclick = generatePdf;
}



function addPlayerRowToDivisionTable(index, player, divisionTable) {
    const letterCell = `<td>${String.fromCharCode(65 + index)}</td>`

    const row = document.createElement('tr');
    row.id = String(player.id);
    const nameCell = document.createElement('td');
    nameCell.className = 'admin-name-cell';
    nameCell.innerText = player.name;

    row.appendChild(nameCell);

    const detailsCell = document.createElement('td');
    detailsCell.className = 'admin-details-cell';

    const phoneNumberDiv = document.createElement('div');
    phoneNumberDiv.className = 'phone-number-div';
    phoneNumberDiv.innerHTML = player.phoneNumber;

    const emailDiv = document.createElement('div');
    emailDiv.className = 'email-div';
    emailDiv.innerHTML = player.email;

    detailsCell.appendChild(phoneNumberDiv);
    detailsCell.appendChild(emailDiv);
    row.appendChild(detailsCell);


    const promoteButtonCell = document.createElement('td');
    const promoteButton = document.createElement('button');
    promoteButton.innerHTML = 'Promote';
    promoteButton.onclick = changeTable.bind(thisArg = promoteButton, -1);
    promoteButtonCell.appendChild(promoteButton);

    const relegateButtonCell = document.createElement('td');
    const relegateButton = document.createElement('button');
    relegateButton.innerHTML = 'Relegate';
    relegateButton.onclick = changeTable.bind(thisArg = relegateButton, 1);
    relegateButtonCell.appendChild(relegateButton);

    const redFlagButtonCell = document.createElement('td');
    const redFlagButton = document.createElement('button');
    redFlagButton.innerHTML = 'Red Flag';
    redFlagButton.onclick = redFlagPlayerToggle.bind(thisArg = redFlagButton)
    redFlagButtonCell.appendChild(redFlagButton);

    row.innerHTML = letterCell + row.innerHTML;
    row.appendChild(promoteButtonCell);
    row.appendChild(relegateButtonCell);
    row.appendChild(redFlagButtonCell);
    divisionTable.appendChild(row);

    if (player.isRedFlagged == true) {
        redFlagPlayerToggle.call(redFlagButton);
    }
}

function redFlagPlayerToggle() {
    const row = this.parentElement.parentElement;

    if (row.classList.contains("red-flag")) {
        row.classList.remove("red-flag");
    } else {
        row.classList.add("red-flag");
    }
}

function changeTable(divisionIndexChange) {
    const row = this.parentElement.parentElement;
    const parentTableId = getTableId(row.parentElement);
    const destinationTableId = Number(parentTableId) + divisionIndexChange;
    if (destinationTableId < 0) {
        console.log('Cannot promote player above top table. Cancelling.')
        return;
    } else if (destinationTableId >= divisionsCount) {
        console.log('Cannot relegate player below bottom table. Cancelling.')
        return;
    }
    const destinationTable = document.getElementById('playerTable' + destinationTableId);

    destinationTable.appendChild(row);
}

function getTableId(tableElement) {
    const idAttribute = tableElement.getAttribute('id');
    const parentTableId = idAttribute.match(/\d+/);
    return parentTableId;
}

function updateDivisions() {
    console.log('Updating player divisions')
    const divisionUpdates = [];
    for (let i = 0; i < divisionsCount; i++) {
        const table = document.getElementById('playerTable' + i);
        table.childNodes.forEach(tr => {
            if (tr.className.includes('table-top-row')) {
                return;
            }
            divisionUpdates.push(new DivisionUpdate(tr.id, i, tr.classList.contains("red-flag")));
        });
    }

    const updateBody = JSON.stringify(divisionUpdates)
    fetch('http://localhost:8080/api/table/update-table', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-type': 'application/json; charset=UTF-8'
        },
        body: updateBody
    })
    .then(() => {
        console.log('Tables updated');
    })
    .catch(error => {
        console.error('Error saving division updates:', error);
        throw error;
    });
}

function newSeason() {
    if (!confirm('Are you sure you want to end the current season and create another?')) {
        return;
    }

    const newSeasonEndDate = document.getElementById('new-season-end-date').value;
    if (newSeasonEndDate === null || newSeasonEndDate === '') {
        alert('Please select a date for the Season end date.');
        return;
    }

    console.log('Creating a new Season, doing promotions, relegations etc.. New end date: ' + newSeasonEndDate);
    fetch('http://localhost:8080/api/table/new-season', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-type': 'application/json; charset=UTF-8'
        },
        body: JSON.stringify(newSeasonEndDate)
    })
    .then(() => {
        console.log('New Season created!');
    })
    .catch(error => console.error('Error creating new season:', error));
}

function generatePdf() {
    try {
        updateDivisions();
    } 
    catch (error) {
        alert("Error while trying to save table before pdf generation: " + error);
    }

    fetch('http://localhost:8080/api/table/generate-pdf', {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => response.blob())
    .then(blob => {
        console.log('Blob returned from fetch');
        var file = window.URL.createObjectURL(blob);
        window.open(file, '_blank').focus;
    })
    .catch(error => console.error('Error while generating pdf', error));

}
