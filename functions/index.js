//export GOOGLE_APPLICATION_CREDENTIALS="/home/dany/FirebaseProjects/firenewsWeb/firenews-92f91-ce72ddd1d8c2.json"

const functions = require("firebase-functions");
const fetch= require("node-fetch");
const admin = require('firebase-admin');
admin.initializeApp();


/*exports.fetchByCategories=functions.https.onRequest(async(req,res)=>{
    const categories=["business","entertainment","general","health","science","sports","technology"];
    categories.forEach(category=>{
        const query='country=mx&category='+category
        getNews(query)
        .then(out=>console.log("Obtained Json",out))
        .catch(err=>{throw err});
    })
    
});*/

exports.fetchBySource=functions.https.onRequest(async(req,res)=>{
    const sources=["cnn-es","el-mundo","infobae","la-nacion","marca"];
    //const sources=["cnn-es","el-mundo"];
    sources.forEach(source=>{
        const query='sources='+source
        getNews(query)
        .then(result=>{
            writeNewsInDB(source, result.articles);
            
        })
        .catch(err=>{throw err});
    });
    res.send('ok');
    
});

const getNews= (query)=>{
    let url='https://newsapi.org/v2/top-headlines?apiKey=ac0805f2465f4f89853f569ad67085cb&'+query;
    return fetch(url)
    .then(res=>res.json())
}

const writeNewsInDB=(source, articles)=>{
    const ref=admin.database().ref('/sources/'+source+'/');
    articles.forEach(article=>{
        //delete article.source;
        article.source=article.source.name
        const reference=ref.push()
        article.id=reference.key
        reference.set(article);
    });
    
}


// // Create and deploy your first functions
// // https://firebase.google.com/docs/functions/get-started
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
