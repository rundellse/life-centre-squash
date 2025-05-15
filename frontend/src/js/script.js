
class Division {
    constructor(divisionNum) {
        this.divisionNum = divisionNum;
        this.players = [];
    }
}


document.addEventListener('DOMContentLoaded', function() {
    const apiUrl = 'http://localhost:8080/api/players';
    const credentials = btoa('user' + ':' + 'password1');
    const tableBlock = document.querySelector('#table-block');
    const divisions = [];

    fetch(apiUrl, {
        // credentials: 'include',
        method: 'GET',
        headers: {
            'Authorization': 'Basic ' + credentials
        }
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
                divisionTable.innerHTML = createDivisionTableTopRow(divisionLength);

                for (let i = 0; i < divisionLength; i++) {
                    let player = division.players[i];
                    addPlayerRowToDivisionTable(i, divisionLength, player, divisionTable);
                }
                tableBlock.appendChild(divisionTable);
            })
        })
        .catch(error => console.error('Error fetching players:', error));
});

function createDivisionTableTopRow(divisionLength) {
    let topRowLetters = "";
    for (let i = 0; i < divisionLength; i++) {
        topRowLetters = topRowLetters + "<td>" + String.fromCharCode(65 + i) + "</td>\n"; // Capitals from A...
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
    const letterCell = `<td>${String.fromCharCode(65 + index)}</td>`

    const row = document.createElement('tr');

    const nameCell = document.createElement('th');
    nameCell.className = 'name-div';
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
            gameCells = gameCells + '<td class="self-game-cell"></td>';
        } else {
            gameCells = gameCells + '<td></td>' 
        }
    }

    row.innerHTML = letterCell + row.innerHTML + gameCells;
    // row.getElementsByClassName('name-div');
    divisionTable.appendChild(row);
}
