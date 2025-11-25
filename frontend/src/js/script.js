let userPlayerId = -1;

class Division {
    constructor(divisionNum) {
        this.divisionNum = divisionNum;
        this.players = [];
    }
}

document.addEventListener('DOMContentLoaded', async function() {
    await checkUserSessionForPermissions();
    await getPlayerForUser();
    configureLogoutButton();
    loadTables();
});

// Checks like this are why this page should be served from the server or be an SPA.
async function checkUserSessionForPermissions() {
    fetch('http://localhost:8080/api/user/roles', {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => {
        if (response.status == 403) {
            window.location = 'login.html';
        }
        return response;
    })
    .then(response => response.json())
    .then(roles => {
        roles.forEach(role => {
            if (role == 'ROLE_ADMIN') {
                document.getElementById('admin-link').removeAttribute('hidden');
            }
        })
    })
    .catch(error => console.error('Error while checking user roles: ', error));

    return Promise.resolve();
}

async function getPlayerForUser() {
    fetch('http://localhost:8080/api/user/player', {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(id => userPlayerId = id)
    .catch(error => console.error('Error while getting player ID: ', error));
}

function configureLogoutButton() {
    const logoutButton = document.getElementById('logout-button');
    logoutButton.onclick = logout;
}

function logout() {
    const logoutUrl = 'http://localhost:8080/api/logout';

    fetch(logoutUrl, {
        method: 'POST',
        credentials: 'include',
    })
    .then(response => {
        if (response.status == 200) {
            alert('Logout completed successfully.');
            window.location = 'login.html'
        } else {
            throw new Error('Logout not completed. Non-OK response code returned: ' + response.status);
        }
    })
    .catch(error => console.error('Error while logging out: ', error));
}

function loadTables() {
    const apiUrl = 'http://localhost:8080/api/players';
    const tableBlock = document.getElementById('table-block');
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
        let divisionNum = 0;

        divisions.forEach(division => {
            const divisionTitle = document.createElement('h2');
            divisionTitle.setAttribute('class', 'table-heading')
            divisionTitle.innerText = getDivisionTitle(divisionNum);
            tableBlock.appendChild(divisionTitle);

            const divisionLength = division.players.length;
            const divisionTable = document.createElement('table');
            divisionTable.setAttribute('id', 'playerTable' + division.divisionNum);
            divisionTable.appendChild(createDivisionTableTopRow(divisionLength));

            let userPlayerIndex = findPlayerIndexIfInDivision(division, userPlayerId);
            for (let i = 0; i < divisionLength; i++) {
                const player = division.players[i];
                addPlayerRowToDivisionTable(i, division, divisionLength, player, divisionTable, userPlayerIndex);
            }
            tableBlock.appendChild(divisionTable);
            divisionNum++;
        })
    })
    .catch(error => console.error('Error fetching players:', error));
}

function findPlayerIndexIfInDivision(division, playerId) {
    for (let i = 0; i < division.players.length; i++) {
        if (division.players[i].id === playerId) {
            return i;
        }
    }
    return -1;
}

function getDivisionTitle(divisionNum) {
    if (divisionNum == 0) {
        return 'PREMIER DIVISION';
    }

    return 'DIVISION ' + divisionNum;
}

function createDivisionTableTopRow(divisionLength) {
    const thead = document.createElement('thead');
    const tr = document.createElement('tr');

    const th = document.createElement('th');
    th.colSpan = 3;
    tr.appendChild(th);
    
    for (let i = 0; i < divisionLength; i++) {
        const td = document.createElement('td');
        td.className = 'top-letter-cell';
        // Capitals from A...
        td.innerText = String.fromCharCode(65 + i);
        tr.appendChild(td);
    }

    thead.appendChild(tr);
    return thead;
}

function addPlayerRowToDivisionTable(index, division, divisionLength, player, divisionTable, userPlayerIndex) {
    const row = document.createElement('tr');
    
    const letterCell = document.createElement('td');
    letterCell.className = 'side-letter-cell';
    letterCell.innerText = String.fromCharCode(65 + index);
    row.appendChild(letterCell);

    const nameCell = document.createElement('th');
    nameCell.className = 'name-cell';
    nameCell.innerText = player.name;
    // const availabilityPopup = document.createElement('div');
    // availabilityPopup.className = 'availability-div';
    // availabilityPopup.innerText = player.availabilityNotes;

    // nameCell.addEventListener('mouseenter', () => {
    //     availabilityPopup.style.display = 'block';
    // });
    // nameCell.addEventListener('mouseleave', () => {
    //     availabilityPopup.style.display = 'none';
    // });

    // nameCell.appendChild(availabilityPopup);
    row.appendChild(nameCell);

    const detailsCell = document.createElement('th');
    detailsCell.className = 'details-cell';

    const phoneNumberDiv = document.createElement('div');
    phoneNumberDiv.className = 'phone-number-div';
    phoneNumberDiv.innerHTML = player.phoneNumber;

    const emailDiv = document.createElement('div');
    emailDiv.className = 'email-div';
    emailDiv.innerHTML = player.email;

    detailsCell.appendChild(phoneNumberDiv);
    detailsCell.appendChild(emailDiv);
    row.appendChild(detailsCell);

    const gameCells = assembleGameCellsForRow(index, division, divisionLength, player.id, userPlayerIndex);
    gameCells.forEach(cell => row.appendChild(cell));

    divisionTable.appendChild(row);
}

function assembleGameCellsForRow(rowIndex, division, divisionLength, rowPlayerId, userPlayerIndex) {
    let gameCells = [];
    // i here is the x-axis, moving along the row
    for (let columnIndex = 0; columnIndex < divisionLength; columnIndex++) {
        const gameCell = document.createElement("td");
        gameCell.className = "game-cell";

        const columnPlayerId = division.players[columnIndex].id;

        if (columnIndex === rowIndex) {
            gameCell.className = gameCell.className + ', self-game-cell';
        } else if (columnIndex === userPlayerIndex) {
            // Other player's points input
            gameCell.appendChild(createGamePointInput(rowPlayerId, columnIndex, columnPlayerId));
        } else if (rowIndex === userPlayerIndex) {
            // Our player's points input
            gameCell.appendChild(createGamePointInput(rowPlayerId, columnIndex, columnPlayerId));
        }

        gameCells.push(gameCell);
    }

    return gameCells;
}

function createGamePointInput(rowPlayerId, columnIndex, columnPlayerId) {
    const input = document.createElement('input');
    input.type = 'text';
    input.className = 'game-score-input';
    input.autocomplete = 'off';
    input.maxLength = 1;
    input.dataset.rowPlayerId = rowPlayerId;
    input.dataset.columnPlayerId = columnPlayerId;

    input.oninput = function() {
        // Only numbers but no spinner, this is simpler than trying to remove spinners on a number input in css
        this.value = this.value.replace(/[^0-9]/g, '');
    };
    input.onchange = updateMatchPoints;

    return input;
}

function updateMatchPoints(event) {
    const matchUrl = 'http://localhost:8080/api/match';
    const requestBody = JSON.stringify({
            homePlayerId: event.target.dataset.rowPlayerId,
            awayPlayerId: event.target.dataset.columnPlayerId,
            points: event.target.value
        });

    console.log(requestBody);

    fetch(matchUrl, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-type': 'application/json; charset=UTF-8'
        },
        body: requestBody
    })
    .then(response => {
        if (response.status != 202) {
            alert('Error while trying to save Match Points, if this continues to occur please contact us (\'About\' page).');
        }
    })
    .catch(error => console.error('Error while logging out: ', error));
}
