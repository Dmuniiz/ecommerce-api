async function buscarComAsyncAwait(url) {
    try {
        const response = await fetch(url);
        console.log("Status da resposta:", response.status);
        console.log(response.headers);

        const dados = await response.json();
        
        console.log("Dados recebidos:", dados);
    }catch (erro) {
        console.error("Erro na requisição ou ao processar os dados:", erro);
    }
}

buscarComAsyncAwait('https://jsonplaceholder.typicode.com/posts/1');