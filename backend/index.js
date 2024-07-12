const express = require("express");
const bodyParser = require("body-parser");
const admin = require("firebase-admin");

const app = express();
app.use(bodyParser.json());
app.use(cors());

// Inisialisasi Firebase Admin SDK
const serviceAccount = require("./admin/mokoli-561f5-firebase-adminsdk-ttpq4-bd272aa01b.json");
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://mokoli-561f5-default-rtdb.firebaseio.com/",
});

// Fungsi untuk mengambil tanggal dari timestamp (format: DD-MM-YYYY)
function getDateFromTimestamp(timestamp) {
  const date = new Date(timestamp);
  const day = String(date.getDate()).padStart(2, "0");
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const year = date.getFullYear();
  return `${day}-${month}-${year}`;
}

let userID = null;

// Endpoint untuk menerima userID dan mengembalikan tokenFCM
app.post("/userID", async (req, res) => {
  //definisi rute metode pos
  const { userID: receivedUserID } = req.body; //ambil userID dari bodyrequest

  if (!receivedUserID) {
    return res.status(400).json({ error: "Missing userID" });
  }

  try {
    // Panggil fungsi untuk mendapatkan tokenFCM berdasarkan userId
    const tokenFCM = await getTokenFCM(receivedUserID);

    if (tokenFCM) {
      // Jika berhasil mendapatkan tokenFCM, simpan userID ke dalam variabel global
      userID = receivedUserID;
      console.log("Token FCM:", tokenFCM, userID);
      res.status(200).json({ message: "Received userID successfully", userID, tokenFCM });
    } else {
      // Jika tokenFCM tidak ditemukan, kirim respon dengan pesan error
      res.status(404).json({ error: "TokenFCM not found for the given userID" });
    }
  } catch (error) {
    // Tangani kesalahan jika terjadi saat mendapatkan tokenFCM
    console.error("Error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
});

// Fungsi untuk mendapatkan nilai tokenFCM dari Firestore berdasarkan userId
async function getTokenFCM(userId) {
  try {
    const userDoc = await admin.firestore().collection("users").doc(userId).get();
    if (userDoc.exists) {
      return userDoc.data().tokenFCM;
    } else {
      return null;
    }
  } catch (error) {
    console.error("Error getting user:", error);
    throw error;
  }
}

// Variabel untuk menyimpan data dari Firebase Realtime Database
let batasanData = null;
let energyData = null;

// Mendengarkan perubahan pada Firebase Realtime Database untuk Batasan
const batasanRef = admin.database().ref("/SensorData/Batasan");
batasanRef.on("value", (snapshot) => {
  batasanData = snapshot.val();
  console.log("Nilai batasanData:", batasanData);
  updateSelisih();
});

// Mendengarkan perubahan pada Firebase Realtime Database untuk Energy
const energyRef = admin.database().ref("/SensorData/Energy");
energyRef.on("value", (snapshot) => {
  energyData = snapshot.val();
  console.log("Nilai energyData:", energyData);
  updateSelisih();
});

async function updateSelisih() {
  if (userID !== null && batasanData !== null && energyData !== null) {
    const selisih = batasanData - energyData;
    console.log("Selisih antara batasanData dan energyData:", selisih);
    if (selisih <= 5) {
      try {
        const tokenFCM = await getTokenFCM(userID);
        if (tokenFCM) {
          const notificationBody = `Sisa token listrik Anda saat ini adalah ${selisih}`;
          const notificationTitle = "PERINGATAN";
          sendNotification(tokenFCM, notificationBody, notificationTitle);
        } else {
          console.error("TokenFCM not found for the given userID:", userID);
        }
      } catch (error) {
        console.error("Error sending notification:", error);
      }
    }
  }
}

// Fungsi untuk mengirim notifikasi menggunakan tokenFCM
async function sendNotification(tokenFCM, body, title) {
  try {
    const response = await fetch("https://fcm.googleapis.com/v1/projects/mokoli-561f5/messages:send", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization:
          "Bearer ya29.a0Ad52N3-jjfrk8ofySFKMKVMcp5vAgab1NCVVpjNcCxfu9N9FnXMHkhLlXX8WENgtA2bXER1dV45NIakn0xMFqj5_dXyEO3g9kiKrOd6s5M9wL1TxkANHO_aSY8sWkBB3IFTYp6FezhuTc4pRO2rWjPJJNSq9iK5_7usaCgYKAecSARASFQHGX2Mif_RPt2kRQLw3YXu5EDk90Q0170",
      },
      body: JSON.stringify({
        message: {
          token: tokenFCM,
          notification: {
            body: body,
            title: title,
          },
        },
      }),
    });

    if (response.ok) {
      console.log("Notification sent successfully:", body);
    } else {
      console.error("Failed to send notification:", response.statusText);
    }
  } catch (error) {
    console.error("Error sending notification:", error);
  }
}

// Endpoint untuk menerima data dari ESP32
app.post("/data", (req, res) => {
  const data = req.body; // inisialisasi variable
  const timestamp = Date.now();
  const date = getDateFromTimestamp(timestamp);

  const sensorDataRef = admin.firestore().collection("SensorData").doc(date);

  sensorDataRef
    .get()
    .then((doc) => {
      if (!doc.exists) {
        return sensorDataRef.set({});
      } else {
        return doc.data();
      }
    })
    .then((existingData) => {
      const newData = {
        Energy: data.energy,
        Timestamp: data.timestamp,
      };

      const newDataIndex = Object.keys(existingData).length.toString();
      existingData[newDataIndex] = newData;

      return sensorDataRef.set(existingData);
    })
    .then(() => {
      console.log("Data saved to Firestore:", data);
      res.status(200).send("Data saved to Firestore");
    })
    .catch((error) => {
      console.error("Error saving data to Firestore:", error);
      res.status(500).send("Error saving data to Firestore");
    });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
