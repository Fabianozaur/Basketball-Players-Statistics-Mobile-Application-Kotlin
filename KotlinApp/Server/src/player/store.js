import dataStore from 'nedb-promise';

export class playerStore {
  constructor({ filename, autoload }) {
    this.store = dataStore({ filename, autoload });
  }
  
  async find(props) {
    return this.store.find(props);
  }
  
  async findOne(props) {
    return this.store.findOne(props);
  }
  
  async insert(player) {
    return this.store.insert(player);
  };
  
  async update(props, player) {
    return this.store.update(props, player);
  }
  
  async remove(props) {
    return this.store.remove(props);
  }
}

export default new playerStore({ filename: './db/player.json', autoload: true });