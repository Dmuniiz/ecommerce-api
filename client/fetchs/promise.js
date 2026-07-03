//trata de enviar a requisição e processar a resposta usando Promises, o que torna o código mais legível e fácil de manter em comparação com callbacks aninhados.
function buscarComPromisse(url){
    return fetch(url)
        .then(response => {
            console.log("Status da resposta:", response.status);
            console.log(response.headers);
            return response.json();
        })
        .catch(erro => {
            console.error("Erro na requisição:", erro);
            throw erro; // Re-throw para que o erro possa ser tratado posteriormente
        });
}

//trata a resposta da requisição usando Promises, o que torna o código mais legível e fácil de manter em comparação com callbacks aninhados.
buscarComPromisse('https://jsonplaceholder.typicode.com/posts/1')
    .then(dados => {
        console.log("Dados recebidos:", dados);
    })
    .catch(erro => {
        console.error("Erro ao processar os dados:", erro);
    });