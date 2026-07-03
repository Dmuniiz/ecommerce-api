interface Post {
    id: number;
    title: string;
}

function fetchDataPromise (url: string): Promise<Post> {
    return fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json() as Promise<Post>;
        })
        .catch(error => {
            console.error("Error fetching data:", error);
            throw error; // Re-throw the error to be handled by the caller
        });
}

for (let i = 1; i <= 5; i++) {
    fetchDataPromise(`https://jsonplaceholder.typicode.com/posts/${i}`)
        .then(post => {
            console.log(post.id);
            console.log(post.title);
        }).catch(error => {
            console.error("Error fetching post:", error);
        });
}