import L from "leaflet";

// Disaster icons with emoji overlay
export function getDisasterIcon(status) {
  let emoji = "⚠️";
  let color = "red";

  if (status === "partial") {
    emoji = "🟠";
    color = "orange";
  } else if (status === "fulfilled") {
    emoji = "✅";
    color = "green";
  }

  return L.divIcon({
    className: "custom-disaster-icon",
    html: `
      <div style="
        pointer-events:auto;
        background:${color};
        border-radius:50%;
        width:28px;
        height:28px;
        display:flex;
        align-items:center;
        justify-content:center;
        font-size:16px;
        color:white;
        border:2px solid white;
        box-shadow:0 0 4px rgba(0,0,0,0.3);
      ">
        ${emoji}
      </div>
    `,
    iconSize: [32, 32],
    iconAnchor: [16, 32],
  });
}

// Reporter location pin (purple 📍)
export const reporterIcon = L.divIcon({
  className: "custom-reporter-icon",
  html: `
    <div style="
      pointer-events:auto;
      background:purple;
      border-radius:50%;
      width:28px;
      height:28px;
      display:flex;
      align-items:center;
      justify-content:center;
      font-size:16px;
      color:white;
      border:2px solid white;
      box-shadow:0 0 4px rgba(0,0,0,0.3);
    ">
      📍
    </div>
  `,
  iconSize: [32, 32],
  iconAnchor: [16, 32],
});

// Responder *location* pin (blue 📍)
export const responderLocationIcon = L.divIcon({
  className: "custom-responder-location-icon",
  html: `
    <div style="
      pointer-events:auto;
      background:blue;
      border-radius:50%;
      width:28px;
      height:28px;
      display:flex;
      align-items:center;
      justify-content:center;
      font-size:16px;
      color:white;
      border:2px solid white;
      box-shadow:0 0 4px rgba(0,0,0,0.3);
    ">
      📍
    </div>
  `,
  iconSize: [32, 32],
  iconAnchor: [16, 32],
});

// Responder *contribution* pin (blue 🤝)
export const responderIcon = L.divIcon({
  className: "custom-responder-icon",
  html: `
    <div style="
      pointer-events:auto;
      background:blue;
      border-radius:50%;
      width:28px;
      height:28px;
      display:flex;
      align-items:center;
      justify-content:center;
      font-size:16px;
      color:white;
      border:2px solid white;
      box-shadow:0 0 4px rgba(0,0,0,0.3);
    ">
      🤝
    </div>
  `,
  iconSize: [32, 32],
  iconAnchor: [16, 32],
});

// Admin icon (gray 🛡️)
export const adminIcon = L.divIcon({
  className: "custom-admin-icon",
  html: `
    <div style="
      pointer-events:auto;
      background:gray;
      border-radius:50%;
      width:28px;
      height:28px;
      display:flex;
      align-items:center;
      justify-content:center;
      font-size:16px;
      color:white;
      border:2px solid white;
      box-shadow:0 0 4px rgba(0,0,0,0.3);
    ">
      🛡️
    </div>
  `,
  iconSize: [32, 32],
  iconAnchor: [16, 32],
});
