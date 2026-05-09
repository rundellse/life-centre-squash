const userRolesUrl = API_CONFIG.API_BASE_URL + '/user/roles';
const playerUrl = API_CONFIG.API_BASE_URL + '/user/player';
const playersDivisionsUrl = API_CONFIG.API_BASE_URL + '/players/divisions';
const logoutUrl = API_CONFIG.API_BASE_URL + '/logout';
const matchUrl = API_CONFIG.API_BASE_URL + '/match';
var isAdmin = false;

let userPlayerId = -1;

class Division {
    constructor(divisionNum) {
        this.divisionNum = divisionNum;
        this.players = [];
    }
}

document.addEventListener('DOMContentLoaded', async function() {
    try {
        await checkUserSessionForPermissions();
        await getPlayerForUser();
        configureLogoutButton();
        await loadTables();

    } catch (error) {
        console.error('Page initialization failed:', error);
    }
});

// Checks like this are why this page should be served from the server or be an SPA.
async function checkUserSessionForPermissions() {
    const response = await fetch(userRolesUrl, {
        method: 'GET',
        credentials: 'include'
    });

    if (response.status === 403) {
        window.location = 'login.html';
        return;
    }

    if (!response.ok) {
        throw new Error(`Failed to fetch roles: ${response.status}`);
    }

    const roles = await response.json();
    roles.forEach(role => {
        if (role === 'ROLE_ADMIN') {
            document.getElementById('admin-link').removeAttribute('hidden');
            document.getElementById('table-admin-link').removeAttribute('hidden');
            isAdmin = true;
        }
    });
}

async function getPlayerForUser() {
    const response = await fetch(playerUrl, {
        method: 'GET',
        credentials: 'include'
    });
    
    if (!response.ok) {
        throw new Error(`Failed to find Player for the current user. They will not be able to save match results. Response status: ${response.status}`);
    }
    
    userPlayerId = await response.json();
}

function configureLogoutButton() {
    const logoutButton = document.getElementById('logout-button');
    logoutButton.onclick = logout;
}

function logout() {
    fetch(logoutUrl, {
        method: 'POST',
        credentials: 'include',
    })
    .then(response => {
        if (response.status == 200) {
            console.log('Logout completed successfully.');
            window.location = 'login.html';
        } else {
            throw new Error('Logout not completed. Non-OK response code returned: ' + response.status);
        }
    })
    .catch(error => console.error('Error while logging out: ', error));
}

async function loadTables() {
    const response = await fetch(playersDivisionsUrl, {
        method: 'GET',
        credentials: 'include'
    })
    const divisions = await response.json();

    // const divisions = groupPlayersByDivision(players);
    renderDivisionTables(divisions);
}

function renderDivisionTables(divisions) {
    const tableBlock = document.getElementById('table-block');
    tableBlock.textContent = '';

    divisions.forEach(division => {
        tableBlock.appendChild(createDivisionSection(division));
    });
}

function createDivisionSection(division) {
    const divisionSection = document.createElement('div');

    const divisionTitle = document.createElement('h2');
    divisionTitle.class = 'table-heading';
    divisionTitle.textContent = getDivisionTitle(division.divisionRank);

    const divisionTable = document.createElement('table');
    divisionTable.id = 'playerTable' + division.divisionNum;
    divisionTable.appendChild(createDivisionTable(division));

    divisionSection.appendChild(divisionTitle);
    divisionSection.appendChild(divisionTable);
    return divisionSection;
}

function createDivisionTable(division) {
    const divisionTable = document.createElement('table');
    divisionTable.id = 'playerTable' + division.divisionNum;
    divisionTable.appendChild(createDivisionTableTopRow(division.players.length));

    division.players.forEach((player, rowIndex) => {
        divisionTable.appendChild(createDivisionTablePlayerRow(division, rowIndex, player));
    });

    return divisionTable;
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
        td.textContent = String.fromCharCode(65 + i);
        tr.appendChild(td);
    }

    thead.appendChild(tr);
    return thead;
}

function createDivisionTablePlayerRow(division, rowIndex, player) {
    const row = document.createElement('tr');

    row.appendChild(createTextCell(String.fromCharCode(65 + rowIndex), 'side-letter-cell'));
    row.appendChild(createTextCell(player.name, 'name-cell'));
    
    const detailsCell = document.createElement('td');
    detailsCell.className = 'details-cell';
    detailsCell.appendChild(createTextCell(player.phoneNumber, 'phone-number-div'));
    detailsCell.appendChild(createTextCell(player.email, 'email-div'));
    row.appendChild(detailsCell);

    const userPlayerIndex = findPlayerIndexIfInDivision(division, userPlayerId);
    division.players.forEach((columnPlayer, columnIndex) => {
        row.appendChild(createGameCell(rowIndex, columnIndex, player.id, columnPlayer.id, userPlayerIndex, player.matchPoints[columnIndex]));
    });

    return row;
}

function createGameCell(rowIndex, columnIndex, rowPlayerId, columnPlayerId, userPlayerIndex, point) {
    var pointValue = point;
    if (point === null || point === '') {
        pointValue = '';
    } 

    const gameCell = document.createElement('td');
    gameCell.className = 'game-cell';

    if (columnIndex === rowIndex) {
        gameCell.className = gameCell.className + ' self-game-cell';
    } else if (isAdmin) {
        // Admins can input anywhere!
        gameCell.appendChild(createGamePointInput(rowPlayerId, columnIndex, columnPlayerId, pointValue));
    } else if (columnIndex === userPlayerIndex) {
        // Other player's points input
        gameCell.appendChild(createGamePointInput(rowPlayerId, columnIndex, columnPlayerId, pointValue));
    } else if (rowIndex === userPlayerIndex) {
        // Our player's points input
        gameCell.appendChild(createGamePointInput(rowPlayerId, columnIndex, columnPlayerId, pointValue));
    } else {
        gameCell.innerText = pointValue;
    }

    return gameCell;
}

function createTextCell(text, className) {
    const td = document.createElement('td');
    td.className = className;
    td.textContent = text;
    return td;
}

function createGamePointInput(rowPlayerId, columnIndex, columnPlayerId, pointValue) {
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

    input.value = pointValue
    return input;
}

function updateMatchPoints(event) {
    const requestBody = JSON.stringify({
            rowPlayerId: event.target.dataset.rowPlayerId,
            columnPlayerId: event.target.dataset.columnPlayerId,
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
    .catch(error => console.error('Error while trying to save Match points: ', error));
}
