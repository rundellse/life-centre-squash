
class Division {
    constructor(divisionNum) {
        this.divisionNum = divisionNum;
        this.players = [];
    }
}

// Checks like this is why this page should be served from the server or be an SPA ideally.
document.addEventListener('DOMContentLoaded', function() {
    fetch('http://localhost:8080/api/user/roles', {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => {
        if (response.status == 403) {
            window.location = 'login.html';
        }
    })
    .then(response => response.json())
    .then(roles => {
        roles.forEach(role => {
            if (role == 'ROLE_ADMIN') {
                document.getElementById('admin-link').removeAttribute('hidden');
            }
        })
    })
    .catch(error => console.error('Error while checking user roles:', error));
});

document.addEventListener('DOMContentLoaded', function() {
    const logoutButton = document.getElementById('logout-button');
    logoutButton.onclick = logout;
});

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
    .catch(error => console.error('Error while logging out:', error));
}

document.addEventListener('DOMContentLoaded', function() {
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

                divisionTable.innerHTML = createDivisionTableTopRow(divisionLength);

                for (let i = 0; i < divisionLength; i++) {
                    let player = division.players[i];
                    addPlayerRowToDivisionTable(i, divisionLength, player, divisionTable);
                }
                tableBlock.appendChild(divisionTable);
                divisionNum++;
            })
        })
        .catch(error => console.error('Error fetching players:', error));
});

function getDivisionTitle(divisionNum) {
    if (divisionNum == 0) {
        return 'PREMIER DIVISION';
    }

    return 'DIVISION ' + divisionNum;
}

function createDivisionTableTopRow(divisionLength) {
    let topRowLetters = "";
    for (let i = 0; i < divisionLength; i++) {
        // Capitals from A...
        topRowLetters = topRowLetters + '<td class="top-letter-cell">' + String.fromCharCode(65 + i) + '</td>\n';
    }

    return `
    <thead>
        <tr>
            <th colspan="3"></th>
            ${topRowLetters}
        </tr>
    </thead>
    `;
}

function addPlayerRowToDivisionTable(index, divisionLength, player, divisionTable) {
    const letterCell = `<td class="side-letter-cell">${String.fromCharCode(65 + index)}</td>`

    const row = document.createElement('tr');

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

    let gameCells = '';
    for (let i = 0; i < divisionLength; i++) {
        if (i === index) {
            gameCells = gameCells + '<td class="game-cell, self-game-cell"></td>';
        } else {
            gameCells = gameCells + '<td class="game-cell"></td>' 
        }
    }

    row.innerHTML = letterCell + row.innerHTML + gameCells;
    // row.getElementsByClassName('name-div');
    divisionTable.appendChild(row);
}
