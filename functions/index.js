//export GOOGLE_APPLICATION_CREDENTIALS="/home/dany/FirebaseProjects/firenewsWeb/firenews-92f91-ce72ddd1d8c2.json"

const functions = require("firebase-functions");
const fetch = require("node-fetch");
const admin = require('firebase-admin');
const { onCall } = require("firebase-functions/v2/https");
const { getAuth } = require("firebase-admin/auth")
admin.initializeApp();




exports.fetchBySource = functions.https.onRequest(async (req, res) => {
  //const sources=["cnn-es","el-mundo","infobae","la-nacion","marca"];
  const sources = ["cnn-es", "el-mundo"];
  sources.forEach(source => {
    const query = 'sources=' + source
    getNews(query)
      .then(result => {
        writeNewsInDB(source, result.articles);

      })
      .catch(err => { throw err });
  });
  res.send('ok');

});

const getNews = (query) => {
  let url = 'https://newsapi.org/v2/top-headlines?apiKey=ac0805f2465f4f89853f569ad67085cb&' + query;
  return fetch(url)
    .then(res => res.json())
}

const writeNewsInDB = (source, articles) => {
  const ref = admin.database().ref('/sources/' + source + '/');
  articles.forEach(article => {
    //delete article.source;
    article.source = article.source.name
    const reference = ref.push()
    article.id = reference.key
    reference.set(article);
  });
}

const axios = require('axios').default;

const MY_REDIRECT_URI = "https://firenews-92f91.web.app/oauth2redirect";
const MY_CLIENT_SECRET = "gdfgadfsgv";
const CLIENT_ID = "swdfgst";
const EXCHANGE_URL = "https://www.linkedin.com/oauth/v2/accessToken";

exports.getIdToken = onCall(async (request) => {
  const authCode = request.data.authCode;
  const qs = require('qs');
  const params = qs.stringify({
    grant_type: "authorization_code",
    code: authCode,
    client_id: CLIENT_ID,
    client_secret: MY_CLIENT_SECRET,
    redirect_uri: MY_REDIRECT_URI
  })
  try {
    const response = await axios.post(EXCHANGE_URL, params);
    const idToken = response.data.id_token;
    console.log({ response });
    if (idToken) {
      const decoded = decodeUserInfo(idToken);
      const uid = decoded.sub;
      delete decoded.sub;
      console.log({ profile: decoded });
      try {
        const customToken = await getAuth().createCustomToken(uid, decoded)
        console.log({ customToken });
        return { idToken: customToken };

      } catch (error) {
        console.log('Error creating custom token:', error);
      };
    }
  } catch (error_1) {
    console.log(error_1);
  }
});

const decodeUserInfo = (idToken) => {
  const jwt_decode = require('jwt-decode');
  const decoded = jwt_decode(idToken);
  console.log(decoded)
  return {
    name: decoded.name,
    given_name: decoded.given_name,
    family_name: decoded.family_name,
    picture: decoded.picture,
    sub: decoded.sub
  };
}


















