console.log("This is script file");

const toggleSidebar = () => {
    if($('.sidebar').is(":visible")){
        //dikh ra hai to use band kar do.
        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");  
    }
    else{
        //show karana hai
        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");  
    }
};

const search=()=>{
    console.log("calling...");
    let query = $("#search-input").val();
    
    if (query == '') {
        $(".search-result").hide();
    }else{
        // search
        console.log(query);
        // sending request to server
        let url = `http://localhost:8080/search/${query}`;

        fetch(url).then((response)=>{
            return response.json();
        })
        .then((data)=>{
            //data fetching
            console.log(data);
            //now we have to parse the data into the html
            let text = `<div class='list-group'>`;

            data.forEach((contact)=>{
                text+=`<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'>${contact.cName}</a>`
            });

            text+=`</div>`;

            $(".search-result").html(text);
            $(".search-result").show();
        });

        
    }
};