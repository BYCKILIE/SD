import {createRoot} from 'react-dom/client'
import App from './App.jsx'
import './index.css'
import 'bootstrap/dist/css/bootstrap.min.css';
import {AuthProvider} from "./context/AuthContext.jsx";
import {StrictMode} from "react";
import {SignupProvider} from "./context/SignupContext.jsx";
import {DevicesProvider} from "./context/DevicesContext.jsx";
import {OwnershipsProvider} from "./context/OwnershipContext.jsx";
import {UsersProvider} from "./context/UsersContext.jsx";
import {ProfilesProvider} from "./context/ProfilesContext.jsx";

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <AuthProvider>
            <SignupProvider>
                <DevicesProvider>
                    <OwnershipsProvider>
                        <UsersProvider>
                            <ProfilesProvider>
                                <App/>
                            </ProfilesProvider>
                        </UsersProvider>
                    </OwnershipsProvider>
                </DevicesProvider>
            </SignupProvider>
        </AuthProvider>
    </StrictMode>
)
