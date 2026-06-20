const divisionsUrl = API_CONFIG.API_BASE_URL + '/players/divisions';
const playersUrl = API_CONFIG.API_BASE_URL + '/players';
const newSeasonUrl = API_CONFIG.API_BASE_URL + '/table/new-season';
const updateTableUrl = API_CONFIG.API_BASE_URL + '/table/update-table';
const generatePdfUrl = API_CONFIG.API_BASE_URL + '/table/generate-pdf';
const seasonEndUrl = API_CONFIG.API_BASE_URL + '/season/end-date';

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


document.addEventListener('DOMContentLoaded', async function() {
    await loadSeasonEnd();
    await populateAdminTable();
    configureSeasonButtons();
});


async function loadSeasonEnd() {
    const response = await fetch(seasonEndUrl, {
        method: 'GET'
    });

    if (!response.ok) {
        throw new Error('Error while trying to find league end-date');
        return;
    }

    const returnedText = await response.text();

    const seasonEndText = document.getElementById('season-end-text');
    seasonEndText.innerText = `Current league end-date is: ${returnedText}`;
}


async function populateAdminTable() {
    const divisions = [];

    try {
        const response = await fetch(divisionsUrl, {
            method: 'GET',
            credentials: 'include'
        });

        const divisions = await response.json();

        divisions.forEach(division => {
            const divisionLength = division.players.length;
            const divisionTable = createNewDivisionTable(division.divisionRank);

            for (let i = 0; i < divisionLength; i++) {
                const player = division.players[i];
                addPlayerRowToDivisionTable(i, player, divisionTable);
            }
        });
    } catch (error) {
        console.error('Error while fetching players:', error);
    }
}

function createNewDivisionTable(divisionRank) {
    const divisionTable = document.createElement('table');
    divisionTable.className = 'admin-division-table';
    divisionTable.setAttribute('id', 'playerTable' + divisionRank);
    divisionTable.innerHTML = `<thead class="table-top-row"><tr><th colspan="6">Division ` + divisionRank + `</th></tr></thead>`;

    const tableBlock = document.getElementById('admin-table-block');
    tableBlock.insertBefore(divisionTable, document.getElementById('save-divisions-button-bottom'));
    divisionsCount++;
    return divisionTable;
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
        console.log('Cannot promote player above top table. Cancelling.');
        return;
    } else if (destinationTableId == divisionsCount) {
        const newTable = createNewDivisionTable(destinationTableId);
    } else if (destinationTableId > divisionsCount) { 
        console.log('Cannot relegate more than one table below the bottom of the divisions. I don\'t know how you did this.');
    }

    const destinationTable = document.getElementById('playerTable' + destinationTableId);
    destinationTable.appendChild(row);

    const bottomTable = document.getElementById('playerTable' + (divisionsCount - 1));
    if (bottomTable.childElementCount < 2) {
        //Bottom table is now empty, so remove it.
        bottomTable.remove();
        divisionsCount--;
    }
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
    fetch(updateTableUrl, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-type': 'application/json; charset=UTF-8'
        },
        body: updateBody
    })
    .then(() => {
        console.log('Tables updated');
        alert('Season tables updated successfully');
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
    fetch(newSeasonUrl, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-type': 'application/json; charset=UTF-8'
        },
        body: JSON.stringify(newSeasonEndDate)
    })
    .then(() => {
        window.alert('New Season created!');
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

    fetch(generatePdfUrl, {
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
