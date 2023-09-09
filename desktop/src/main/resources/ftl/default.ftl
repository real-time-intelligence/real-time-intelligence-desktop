<!DOCTYPE html>
<html>
<head>
    <style>
        h1 {
            color: #ccc;
        }

        table tr td {
            text-align: center;
            border: 1px solid gray;
            padding: 4px;
        }

        table tr th {
            background-color: #84C7FD;
            color: #fff;
            width: 100px;
        }

        .itext {
            color: #84C7FD;
            font-weight: bold;
        }

        .description {
            color: gray;
        }
    </style>
    <title>Report</title>
    <meta charset="utf-8"/>
    <link href="style.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<h1>Report for the period </h1>
<h2>${dateFrom} - ${dateTo}</h2>

<#list templatesInfos as templateInfo>
<table>
    <tr>
        <th class="label">Profile</th>
        <td>${profileName}</td>
    </tr>
    <tr>
        <th>Task</th>
        <td>${taskName}</td>
    </tr>
    <tr>
        <th>Query</th>
        <td>${queryName}</td>
    </tr>
</table>
    <p><span class="itext"> ${templateInfo.nameCard}  </span></p>
    <p><span class="description"> ${templateInfo.description}</span></p>
    <img class="img" src="${templateInfo.pathChart}" alt="chart"></img>
</#list>

</body>
</html>