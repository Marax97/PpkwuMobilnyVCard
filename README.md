
GetWeeiaEmployees

Get all weeia employees by their name on page with button to download theirs data as vcard

    URL
    /ppkwu/search/{username}
    Method:
    GET
    URL Params
    Username
    Success Response:
    Page with employess name and clicable button to show vcard with their info
    o Code: 200
    Error Response:
    o Code: 404 NOT FOUND
    Content: { error : “User not found” }
    Sample Call:
    https://frozen-lowlands-41205.herokuapp.com/ppkwu/search/wajman

GetEmployeeProfile

Download the employee data to vcard by id

    URL
    /ppkwu/vcard/{id}
    Method:
    GET
    URL Params
    id - (int) employeee id
    Success Response:
    Automatically download the vard as emplopyee.vcf file
    o Code: 200
    Error Response:
    o Code: 404 NOT FOUND
    Content: { error : “User not found” }
    Sample Call:
    https://frozen-lowlands-41205.herokuapp.com/ppkwu/vcard/1449
