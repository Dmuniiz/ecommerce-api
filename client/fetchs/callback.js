const XMLHttpRequest = require('xhr2');

// trata de enviar a requisição e processar a resposta usando callbacks, 
// o que pode levar a um código mais complexo e difícil de manter,
//  especialmente quando há múltiplas operações assíncronas encadeadas (conhecido como "callback hell").
function buscarComCallback(url, callback) {    
    const xhr = new XMLHttpRequest();

    xhr.open('GET', url); // Configura a requisição (método e URL)

    xhr.onload = () => {
        if(xhr.status >= 200 && xhr.status < 300) {
            try{
                const status = xhr.status;
                console.log("Status da resposta:", status);

                const headers = xhr.getAllResponseHeaders();
                console.log(headers);

                const dados = JSON.parse(xhr.responseText);
                callback(null, dados); // Chama o callback com os dados
            }catch(erro) {
                callback(new Error('Erro ao analisar JSON'), null); // Chama o callback com o erro de análise
            }
        }else{
             // Erro HTTP (ex: 404, 500)
            callback(new Error(`Erro HTTP: ${xhr.status}`), null);
        }
    };

    xhr.onerror = () => {
        // Erro de rede
        callback(new Error('Erro de rede'), null);
    };
    xhr.send(); // Envia a requisição
}


buscarComCallback('https://jsonplaceholder.typicode.com/posts/1', (erro, dados) => {
    if(erro) {
        console.error("Erro na requisição:", erro);
    } else {
        console.log("Dados recebidos:", dados);
    }
})
