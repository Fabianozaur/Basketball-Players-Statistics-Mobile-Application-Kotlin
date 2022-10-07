import Router from 'koa-router';
import playerStore from './store';
import { broadcast } from "../utils";

export const router = new Router();

function makeid(length) {
  var result           = '';
  var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  var charactersLength = characters.length;
  for ( var i = 0; i < length; i++ ) {
    result += characters.charAt(Math.floor(Math.random() *
        charactersLength));
  }
  return result;
}

router.get('/', async (ctx) => {
  const response = ctx.response;
  const userId = ctx.state.user._id;
  response.body = await playerStore.find({ userId });
  response.status = 200; // ok
});

router.get('/:id', async (ctx) => {
  const userId = ctx.state.user._id;
  const player = await playerStore.findOne({ _id: ctx.params.id });
  const response = ctx.response;
  if (player) {
    if (player.userId === userId) {
      response.body = player;
      response.status = 200; // ok
    } else {
      response.status = 403; // forbidden
    }
  } else {
    response.status = 404; // not found
  }
});

const createplayer = async (ctx, player, response) => {
  try {
    const userId = ctx.state.user._id;
    player.userId = userId;
    player._id = makeid(10)
    response.body = await playerStore.insert(player);
    response.status = 201; // created
    console.log(player)
    broadcast(userId, { type: 'created', payload: player });
  } catch (err) {
    response.body = { message: err.message };
    response.status = 400; // bad request
  }
};

router.post('/', async ctx => await createplayer(ctx, ctx.request.body, ctx.response));

router.put('/:id', async (ctx) => {
  const player = ctx.request.body;
  const id = ctx.params.id;
  const playerId = player._id;
  const response = ctx.response;
  if (playerId && playerId !== id) {
    response.body = { message: 'Param id and body _id should be the same' };
    response.status = 400; // bad request
    return;
  }
  if (!playerId) {
    await createplayer(ctx, player, response);
  } else {
    const userId = ctx.state.user._id;
    player.userId = userId;
    const updatedCount = await playerStore.update({ _id: id }, player);
    if (updatedCount === 1) {
      response.body = player;
      response.status = 200; // ok
      broadcast(userId, { type: 'updated', payload: player });
    } else {
      response.body = { message: 'Resource no longer exists' };
      response.status = 405; // method not allowed
    }
  }
});

router.del('/:id', async (ctx) => {
  const userId = ctx.state.user._id;
  const player = await playerStore.findOne({ _id: ctx.params.id });
  if (player && userId !== player.userId) {
    ctx.response.status = 403; // forbidden
  } else {
    await playerStore.remove({ _id: ctx.params.id });
    ctx.response.status = 204; // no content
  }
  
});
