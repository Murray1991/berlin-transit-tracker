import './App.css'
import * as React from "react";
import {useEffect, useState} from "react";

let routeId: number = 0;

interface User {
    uuid: string;
}

let userPromise: Promise<User> | undefined;

function getOrCreateUser(): Promise<User> {
    const savedId = localStorage.getItem("user-id");
    if (savedId) {
        userPromise = Promise.resolve({uuid: savedId});
    }

    if (!userPromise) {
        userPromise = fetch('/api/users', {
            method: 'POST',
            headers: {
                Accept: 'application/json',
            }
        }).then(async (response) => {
            if (!response.ok) {
                throw new Error(`Could not create user: ${response.status}`);
            }
            return await response.json() as User;
        }).then((user: User) => {
            localStorage.setItem("user-id", user.uuid);
            return user;
        }).catch((error) => {
            userPromise = undefined;
            throw error
        });
    }

    return userPromise;
}

function App() {
    const stations = ['Alexanderplatz', 'Hermannplatz'];
    const [from, setFrom] = useState('');
    const [to, setTo] = useState('');
    const [time, setTime] = useState('08:30');
    const [alerts, setAlerts] = useState<string[]>([]);
    const [connected, setConnected] = useState(false);
    const [user, setUser] = useState<User | null>(null);

    useEffect(() => {
        getOrCreateUser()
            .then(setUser)
            .catch(error => console.error('Could not create user ', error));
    }, [])

    useEffect(() => {
        if (!user) {
            return;
        }

        const url = new URL(`ws://${window.location.host}/ws/${user.uuid}`);
        const ws = new WebSocket(url);
        ws.onopen = () => setConnected(true);
        ws.onclose = () => setConnected(false);
        ws.onerror = () => setConnected(false);
        ws.onmessage = (event) => {
            setAlerts((prevAlerts) => [...prevAlerts, event.data]);
        };
        return () => ws.close();
    }, [user]);

    type Commute = { id: number; from: string; to: string; time: string };
    const [commutes, setCommutes] = useState<Commute[]>(
        () => {
            try {
                return JSON.parse(localStorage.getItem('routes') ?? '[]') as Commute[];
            } catch {
                return [];
            }
        }
    );
    const saveRoute = (event: React.SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();

        if (!from || !to || from == to) {
            return;
        }

        // TODO: send POST request to backend

        const next = [{id: routeId++, from, to, time}, ...commutes];
        setCommutes(next);

        localStorage.setItem('routes', JSON.stringify(next));
        setFrom('');
        setTo('');
    };

    const deleteRoute = (id: number) => {
        var filtered = commutes.filter((comm) => comm.id !== id);
        setCommutes(filtered);
        localStorage.setItem('routes', JSON.stringify(filtered));
    }

    return (
        <>
            <header className="topbar">
                <h1> Berlin Transit Tracker </h1>
                <div className="connection" aria-live="polite"><span
                    className={connected ? 'dot connected' : 'dot'}/>WebSocket {connected ? 'connected' : 'disconnected'}
                </div>
            </header>

            <main className="layout">
                <section className="panel">

                    <h2>Add a commute</h2>

                    <form onSubmit={saveRoute}>
                        <label htmlFor="from">Station A</label>
                        <select id="from" value={from} onChange={event => setFrom(event.target.value)} required>
                            <option value="" disabled>Select a station</option>
                            {stations.map(station => <option key={station}>{station}</option>)}
                        </select>

                        <label htmlFor="to">Station B</label>
                        <select id="to" value={to} onChange={event => setTo(event.target.value)} required>
                            <option value="" disabled>Select a station</option>
                            {stations.map(station => <option key={station}>{station}</option>)}
                        </select>

                        <label htmlFor="time">Departure time</label>
                        <input id="time" type="time" value={time} onChange={event => setTime(event.target.value)}
                               required/>

                        <button type="submit" disabled={!from || !to || from == to}>
                            Save Route
                        </button>
                    </form>
                </section>

                <section className="commutes">
                    <h2>Your commutes</h2>
                    {commutes.length === 0 ? <p className="empty">No saved commutes yet.</p> : commutes.map(commute =>
                        <article className="commute-card" key={commute.id}>
                            <div><strong>{commute.from} → {commute.to}</strong><p>{commute.time}</p></div>
                            <button className="delete-button" onClick={() => deleteRoute(commute.id)}>Delete</button>
                        </article>)}
                </section>

                <section className="alerts" aria-live="polite">
                    <h2>Live alerts</h2>
                    {alerts.length == 0 ? <p className="empty">No alerts yet</p> : (
                        <ul>
                            {alerts.map((alert, index) => (
                                <li key={index}>{alert}</li>
                            ))}
                        </ul>
                    )}
                </section>
            </main>
        </>
    )
}

export default App
