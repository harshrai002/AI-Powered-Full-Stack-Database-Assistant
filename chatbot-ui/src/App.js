import React from "react";
import Chatbot from "react-chatbot-kit";
import "react-chatbot-kit/build/main.css";
import "./chatbot-custom.css";

// 1. Create a simple message parser
function MessageParser({ children, actions }) {
  const parse = (message) => {
    actions.handleUserMessage(message);
  };
  return React.cloneElement(children, { parse });
}

// 2. Create a simple action provider
function ActionProvider({ createChatBotMessage, setState, children }) {
  const handleUserMessage = async (message) => {
    try {
      const response = await fetch("http://localhost:8080/api/chat/query", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ message }), // send message as JSON
      });

      const data = await response.json();
      const botMessage = createChatBotMessage(data.response);

      setState((prev) => ({
        ...prev,
        messages: [...prev.messages, botMessage],
      }));
    } catch (error) {
      console.error("Error:", error);
      const botMessage = createChatBotMessage("Sorry, something went wrong.");
      setState((prev) => ({
        ...prev,
        messages: [...prev.messages, botMessage],
      }));
    }
  };

  return React.cloneElement(children, { actions: { handleUserMessage } });
}



// 3. Create a config for the chatbot
const config = {
  initialMessages: [
    {
      type: "bot",
      id: "1",
      message: "Welcome! Ask me anything about students.",
    },
  ],
  botName: "StudentBot",
  customComponents: {
    botAvatar: () => (
      <div style={{
        backgroundColor: "#007bff",
        color: "white",
        borderRadius: "50%",
        width: 40,
        height: 40,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontWeight: "bold"
      }}>
        B
      </div>
    ),
    userAvatar: () => (
      <div style={{
        backgroundColor: "#28a745",
        color: "white",
        borderRadius: "50%",
        width: 40,
        height: 40,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontWeight: "bold"
      }}>
        U
      </div>
    ),
  },
};

function App() {
  return (
    <div>
      <Chatbot
        config={config}
        messageParser={MessageParser}
        actionProvider={ActionProvider}
      />
    </div>
  );
}

export default App;
